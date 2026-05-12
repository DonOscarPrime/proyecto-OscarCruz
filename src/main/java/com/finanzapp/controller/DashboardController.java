package com.finanzapp.controller;

import com.finanzapp.dao.MovimientoDAO;
import com.finanzapp.dao.ObjetivoDAO;
import com.finanzapp.model.Movimiento;
import com.finanzapp.model.Objetivo;
import com.finanzapp.model.Usuario;
import com.finanzapp.renderer.DashboardRenderer;
import com.finanzapp.service.DashboardService;
import com.finanzapp.service.ObjetivoService;
import com.finanzapp.util.Formateador;
import com.finanzapp.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controlador del panel principal  de Fox Wallet.
 * Aporta el resumen financiero mensual.
 */
public class DashboardController implements Initializable, MainController.ControladorSecundario {

    @FXML private Label EtiquetaSaludo;
    @FXML private Label EtiquetaDate;
    @FXML private Label estadisticaSaldo;
    @FXML private Label estadisIngreso;
    @FXML private Label estadisticaGasto;
    @FXML private Label estadisAhorro;
    @FXML private Label etiquetaIngresos;
    @FXML private Label etiquetaGastos;
    @FXML private Canvas donut;
    @FXML private Canvas diaBarras;
    @FXML private VBox   donutTexto;
    @FXML private VBox   txList;
    @FXML private HBox   objetivosBox;

    private final MovimientoDAO     movDAO           = new MovimientoDAO();
    private final ObjetivoDAO       objetivoDAO      = new ObjetivoDAO();
    private final DashboardService  dashboardService = new DashboardService();
    private final ObjetivoService   objetivoService  = new ObjetivoService();
    private final DashboardRenderer renderer         = new DashboardRenderer();

    private static final String[] COLORES_DONUT = {
        "#1D9E75", "#BA7517", "#185FA5", "#8B5CF6", "#D85A30", "#6B6A65"
    };

    private MainController main;

    @Override
    public void setMain(MainController main) {
        this.main = main;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Session sesionDashboard = Session.getInstance();
        Usuario usuario = sesionDashboard.getUsuarioActual();
        enseniarSaludo(usuario);
        mostrarFecha();
        cargarMensual(usuario);
        CargarTransacciones(usuario);
        cargarObjetivos(usuario);
    }


    private void enseniarSaludo(Usuario usuario) {
        String nombreCompleto = usuario.getNombre();
        String[] partesNombre = nombreCompleto.split(" ");
        String primerNombre = partesNombre[0];
        String textoBienvenida = "Hola, " + primerNombre + " 👋";
        EtiquetaSaludo.setText(textoBienvenida);
    }

    private void mostrarFecha() {
        Locale localEspanol = new Locale("es");
        String patronFecha = "EEEE, d 'de' MMMM 'de' yyyy";
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern(patronFecha, localEspanol);
        LocalDate hoy = LocalDate.now();
        String fechaFormateada = hoy.format(formatoFecha);
        EtiquetaDate.setText(fechaFormateada);
    }

    private void cargarMensual(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        int anioActual = hoy.getYear();
        int mesActual = hoy.getMonthValue();
        int idUsuario = usuario.getId();
        List<Movimiento> movimientosMes = movDAO.obtenerMovimientosMes(idUsuario, anioActual, mesActual);

        double ingresos = dashboardService.calcularTotalIngreso(movimientosMes);
        double gastos   = movDAO.obtenerTotalPorTipoYMes(idUsuario, anioActual, mesActual, "gasto");
        double saldo    = ingresos - gastos;
        double tasa     = dashboardService.calcularTasaAhorro(ingresos, saldo);

        actualizarEtiquetas(ingresos, gastos, saldo, tasa);
        crearDonut(movimientosMes);
        int idUsuarioBarras = usuario.getId();
        hacerBarras(idUsuarioBarras, hoy);
    }

    private void actualizarEtiquetas(double ingresos, double gastos, double saldo, double tasa) {
        String saldoFormateadoSinSimbolo = Formateador.moneda(saldo);
        String saldoFormateado = saldoFormateadoSinSimbolo + "€";
        estadisticaSaldo.setText(saldoFormateado);
        estadisticaSaldo.getStyleClass().removeAll("stat-up", "stat-down");
        if (saldo >= 0) {
            estadisticaSaldo.getStyleClass().add("stat-up");
        } else {
            estadisticaSaldo.getStyleClass().add("stat-down");
        }

        String ingresosFormateados = Formateador.moneda(ingresos);
        String textoIngresos = "+" + ingresosFormateados + "€";
        estadisIngreso.setText(textoIngresos);
        etiquetaIngresos.setText("este mes");
        String gastosFormateados = Formateador.moneda(gastos);
        String textoGastos = "-" + gastosFormateados + "€";
        estadisticaGasto.setText(textoGastos);
        etiquetaGastos.setText("este mes");
        String tasaFormateada = Formateador.porcentaje(tasa);
        estadisAhorro.setText(tasaFormateada);

        estadisAhorro.getStyleClass().removeAll("stat-up", "stat-amber", "stat-down");
        if (tasa >= 20) {
            estadisAhorro.getStyleClass().add("stat-up");
        } else if (tasa >= 0) {
            estadisAhorro.getStyleClass().add("stat-amber");
        } else {
            estadisAhorro.getStyleClass().add("stat-down");
        }
    }

    private void CargarTransacciones(Usuario usuario) {
        int idUsuario = usuario.getId();
        List<Movimiento> ultimos = movDAO.obtenerUltimosMovimientos(idUsuario, 5);
        txList.getChildren().clear();

        for (Movimiento m : ultimos) {
            txList.getChildren().add(renderer.crearFilaTransaccion(m));
        }

        if (ultimos.isEmpty()) {
            txList.getChildren().add(renderer.crearMensajeVacioTransacciones());
        }
    }

    private void cargarObjetivos(Usuario usuario) {
        int idUsuario = usuario.getId();
        List<Objetivo> todos = objetivoDAO.obtenerObjetivosUsuario(idUsuario);
        objetivosBox.getChildren().clear();
        int mostrados = 0;
        for (Objetivo obj : todos) {
            if (mostrados >= 3) {
                break;
            }
            if (!obj.iscompletado()) {
                objetivosBox.getChildren().add(renderer.crearTarjetaObjetivo(obj));
                mostrados++;
            }
        }
    }
    // Graficos

    private void crearDonut(List<Movimiento> movimientos) {
        Map<String, Double> gastosCategoria = dashboardService.agruparPorCategoria(movimientos);
        double totalGastos = dashboardService.calcularGastos(gastosCategoria);

        if (totalGastos == 0) {
            return;
        }

        GraphicsContext gc = donut.getGraphicsContext2D();
        gc.clearRect(0, 0, 120, 120);

        hacerSectoresDonut(gc, gastosCategoria, totalGastos);
        dibujarHuecoDonut(gc, totalGastos);
        escribirLeyenda(gastosCategoria);
    }

    private void hacerSectoresDonut(GraphicsContext gc, Map<String, Double> datos, double total) {
        double anguloInicio = -90;
        int indiceColor = 0;

        for (Map.Entry<String, Double> entrada : datos.entrySet()) {
            double valorCategoria = entrada.getValue();
            double proporcion = valorCategoria / total;
            double arco = proporcion * 360;
            int longitudArrayColores = COLORES_DONUT.length;
            int indiceColorCiclico = indiceColor % longitudArrayColores;
            String colorHex = COLORES_DONUT[indiceColorCiclico];
            Color colorSector = Color.web(colorHex);
            gc.setFill(colorSector);
            double arcoNegativo = -arco;
            gc.fillArc(5, 5, 110, 110, anguloInicio, arcoNegativo, javafx.scene.shape.ArcType.ROUND);
            anguloInicio = anguloInicio - arco;
            indiceColor++;
        }
    }

    private void dibujarHuecoDonut(GraphicsContext gc, double totalGastos) {
        Session sesionActual = Session.getInstance();
        boolean modoOscuro = sesionActual.isDarkMode();

        if (modoOscuro) {
            Color colorFondoHuecoOscuro = Color.web("#131211");
            gc.setFill(colorFondoHuecoOscuro);
        } else {
            Color colorFondoHuecoClaro = Color.web("#F7F6F3");
            gc.setFill(colorFondoHuecoClaro);
        }
        gc.fillOval(30, 30, 60, 60);

        if (modoOscuro) {
            Color colorTextoTotalOscuro = Color.web("#EEECE7");
            gc.setFill(colorTextoTotalOscuro);
        } else {
            Color colorTextoTotalClaro = Color.web("#1A1916");
            gc.setFill(colorTextoTotalClaro);
        }
        Font fuenteTotal = Font.font("System", FontWeight.BOLD, 14);
        gc.setFont(fuenteTotal);
        String totalGastosFormateado = Formateador.moneda(totalGastos);
        String textoTotalGastos = totalGastosFormateado + "€";
        double xTextoGastos = 35;
        double yTextoGastos = 57;
        gc.fillText(textoTotalGastos, xTextoGastos, yTextoGastos);

        Font fuenteSubtotal = Font.font("System", 9);
        gc.setFont(fuenteSubtotal);
        if (modoOscuro) {
            Color colorSubtotalOscuro = Color.web("#6B6A65");
            gc.setFill(colorSubtotalOscuro);
        } else {
            Color colorSubtotalClaro = Color.web("#A09F9B");
            gc.setFill(colorSubtotalClaro);
        }
        gc.fillText("total", 48, 68);
    }

    private void escribirLeyenda(Map<String, Double> gastosCategoria) {
        donutTexto.getChildren().clear();
        int indiceColor = 0;

        for (Map.Entry<String, Double> entrada : gastosCategoria.entrySet()) {
            int longitudArrayColoresLeyenda = COLORES_DONUT.length;
            int indiceColorCiclico = indiceColor % longitudArrayColoresLeyenda;
            String color = COLORES_DONUT[indiceColorCiclico];
            String nombreCategoria = entrada.getKey();
            double valorCategoria = entrada.getValue();
            HBox fila = renderer.crearFilaLeyendaDonut(nombreCategoria, valorCategoria, color);
            donutTexto.getChildren().add(fila);
            indiceColor++;
        }
    }

    private void hacerBarras(int idUsuario, LocalDate hoy) {
        String[] etiquetasMeses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        String[] labels = new String[6];
        double[] serieIngresos = new double[6];
        double[] serieGastos   = new double[6];

        for (int i = 5; i >= 0; i--) {
            LocalDate fechaMes = hoy.minusMonths(i);
            int anioMes = fechaMes.getYear();
            int numMes = fechaMes.getMonthValue();
            List<Movimiento> movimientosMes = movDAO.obtenerMovimientosMes(idUsuario, anioMes, numMes);

            int indice = 5 - i;
            int indiceMesEnArray = numMes - 1;
            labels[indice] = etiquetasMeses[indiceMesEnArray];
            serieIngresos[indice] = dashboardService.calcularTotalIngreso(movimientosMes);
            serieGastos[indice]   = dashboardService.calcularTotalGastosMes(movimientosMes);
        }

        double valorMaximo = calcularMaximo(serieIngresos, serieGastos);
        pintarBarras(serieIngresos, serieGastos, labels, valorMaximo);
    }

    private double calcularMaximo(double[] ingresos, double[] gastos) {
        double maximo = 0;
        for (int i = 0; i < 6; i++) {
            if (ingresos[i] > maximo) {
                maximo = ingresos[i];
            }
            if (gastos[i] > maximo) {
                maximo = gastos[i];
            }
        }
        if (maximo == 0) {
            maximo = 1000;
        }
        return maximo;
    }

    private void pintarBarras(double[] ing, double[] gast, String[] labels, double maximo) {
        GraphicsContext gc = diaBarras.getGraphicsContext2D();
        gc.clearRect(0, 0, 340, 120);

        double anchoBarra  = 22;
        double separacion  = 12;
        double alturaMaxima = 90;
        double inicioX     = 20;

        for (int i = 0; i < 6; i++) {
            double anchoDosBarras = anchoBarra * 2;
            double grupoPorMes = anchoDosBarras + separacion + 4;
            double posX = inicioX + i * grupoPorMes;
            double proporcionIngreso = ing[i] / maximo;
            double alturaIngreso = proporcionIngreso * alturaMaxima;
            double proporcionGasto = gast[i] / maximo;
            double alturaGasto = proporcionGasto * alturaMaxima;

            double yIngreso = alturaMaxima - alturaIngreso;
            Color colorBarraIngreso = Color.web("#1D9E75", 0.85);
            gc.setFill(colorBarraIngreso);
            gc.fillRoundRect(posX, yIngreso, anchoBarra, alturaIngreso, 4, 4);

            double xGasto = posX + anchoBarra + 2;
            double yGasto = alturaMaxima - alturaGasto;
            Color colorBarraGasto = Color.web("#D85A30", 0.65);
            gc.setFill(colorBarraGasto);
            gc.fillRoundRect(xGasto, yGasto, anchoBarra, alturaGasto, 4, 4);

            Session sesionActual = Session.getInstance();
            if (sesionActual.isDarkMode()) {
                Color colorEtiquetaOscuro = Color.web("#6B6A65");
                gc.setFill(colorEtiquetaOscuro);
            } else {
                Color colorEtiquetaClaro = Color.web("#A09F9B");
                gc.setFill(colorEtiquetaClaro);
            }
            Font fuenteEtiqueta = Font.font("System", 9);
            gc.setFont(fuenteEtiqueta);
            double xEtiquetaDouble = posX + 3;
            double yEtiquetaDouble = alturaMaxima + 14;
            float xEtiqueta = (float) xEtiquetaDouble;
            float yEtiqueta = (float) yEtiquetaDouble;
            gc.fillText(labels[i], xEtiqueta, yEtiqueta);
        }
    }

    @FXML
    void irGastos() {
        if (main != null) {
            main.showGastos();
        }
    }

    @FXML
    void irObjetivos() {
        if (main != null) {
            main.showObjetivos();
        }
    }

    @FXML
    void irHistorial() {
        if (main != null) {
            main.showHistorial();
        }
    }
}
