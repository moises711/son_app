# SAM - Sistema de Administración para Bordado y Planchado

SAM es una aplicación Android para gestionar servicios de bordado y planchado con una interfaz moderna y funcional.

## Características Principales

- **Tema Oscuro**: Interfaz con tema oscuro y bordes blancos para una mejor experiencia visual.
- **Tres Widgets Principales**:
  - **Bordado y Planchado**: Registro de servicios de bordado y planchado con manejo de adelantos.
  - **Planchado de Chompas**: Gestión específica para servicios de planchado de chompas.
  - **Planchado de Ponchos**: Seguimiento para servicios de planchado de ponchos.
- **Generación de PDF**: Creación automática de reportes en formato PDF.
- **Persistencia de Datos**: Almacenamiento local usando Room Database.
- **Estadísticas**: Visualización de saldo general e ingresos diarios.

## Arquitectura

La aplicación está construida siguiendo el patrón MVVM (Model-View-ViewModel) utilizando las siguientes tecnologías:

- **Kotlin**: Lenguaje de programación principal.
- **Jetpack Compose**: Para la interfaz de usuario moderna y declarativa.
- **Room**: Para la persistencia de datos local.
- **ViewModel**: Para la lógica de negocio y estado de la aplicación.
- **Coroutines**: Para operaciones asincrónicas.
- **iTextPDF**: Para la generación de documentos PDF.

## Estructura del Proyecto

- **ui/theme**: Configuración del tema oscuro personalizado.
- **model**: Clases de datos y entidades.
- **viewmodel**: Lógica de negocio y estado de la aplicación.
- **components**: Componentes reutilizables de UI (widgets).
- **pdf**: Generador de documentos PDF.
- **data**: Configuración de Room Database y DAOs.

## Uso

1. **Widget de Bordado y Planchado**:
   - Seleccione el tipo de servicio (Bordado o Planchado).
   - Ingrese la cantidad de piezas.
   - Presione "Registrar" para guardar el servicio.
   - Use el botón "PDF" para generar un reporte y registrar un pago.

2. **Widget de Planchado de Chompas**:
   - Toque en cualquier parte del widget para registrar una cantidad.
   - Visualice el saldo acumulado.
   - Use el botón "Registrar pago" para registrar un pago.

3. **Widget de Planchado de Ponchos**:
   - Ingrese la cantidad de ponchos en el campo.
   - El saldo cambia de color según su estado (positivo, negativo o neutral).
   - Registre pagos según sea necesario.

4. **Barra Superior**:
   - Muestra el saldo general (verde) y los ingresos diarios (azul).

## Requisitos

- Android 6.0 (API level 24) o superior.
- Permisos de almacenamiento para guardar archivos PDF.

## Licencia

Este proyecto está bajo licencia MIT.