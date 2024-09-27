import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

fun main() {

    val rutaFichero = Path.of("src/main/resources/calificaciones.csv")

    fun comprobarFichero(ruta: Path): Boolean {
        if (ruta.exists()) {
            return true
        } else {
            Files.createDirectories(ruta.parent)
            Files.createFile(ruta)
            return false
        }
    }

    fun getCalificaciones(ruta: Path): MutableList<MutableMap<String, String>> {
        var mapaCalificaciones = mutableListOf<MutableMap<String, String>>()
        if (comprobarFichero(ruta)) {
            val br: BufferedReader = Files.newBufferedReader(ruta)
            val headers = br.readLine().split(";")
            br.useLines { lines ->
                lines.forEach { line ->
                    val values = line.split(";")
                    val mapaAlumno = mutableMapOf<String, String>()
                    for (i in values.indices) {
                        mapaAlumno[headers[i]] = (if (values[i].isBlank()) {
                            0.0
                        } else {
                            values[i]
                        }).toString()
                    }
                    mapaCalificaciones.add(mapaAlumno)
                }
            }
        }
        mapaCalificaciones = mapaCalificaciones.sortedBy { it["Apellidos"]}.toMutableList()
        /*for(i in mapaCalificaciones) {
            println(i)
        }
         */
        return mapaCalificaciones
    }


    fun agregarCalificaciones(listaAlumnos: MutableList<MutableMap<String, String>>): MutableList<MutableMap<String, String>>{
        val listaAlumnosNotaFinal = mutableListOf<MutableMap<String, String>>()
        for (alumno in listaAlumnos) {
            val parcial1 = if (alumno["Parcial1"] != null && (alumno["Parcial1"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                alumno["Ordinario1"]?.replace(",", ".")?.toDouble() ?: 0.0
            } else {
                alumno["Parcial1"]?.replace(",", ".")?.toDouble() ?: 0.0
            }

            val parcial2 = if (alumno["Parcial2"] != null && (alumno["Parcial2"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                alumno["Ordinario2"]?.replace(",", ".")?.toDouble() ?: 0.0
            } else {
                alumno["Parcial2"]?.replace(",", ".")?.toDouble() ?: 0.0
            }

            val practica = if (alumno["Practicas"] != null && (((alumno["Practicas"]?.replace(",", ".")?.toDouble()
                    ?: 0.0) <= (alumno["OrdinarioPracticas"]?.replace(",", ".")?.toDouble() ?: 0.0)))
            ) {
                alumno["OrdinarioPracticas"]?.replace(",", ".")?.toDouble() ?: 0.0
            } else {
                alumno["Practicas"]?.replace(",", ".")?.toDouble() ?: 0.0
            }

            val notaFinal = (((parcial1 + parcial2) * 0.3) + (practica * 0.4)).toString()

            alumno["NotaFinal"] = notaFinal
            listaAlumnosNotaFinal.add(alumno)
        }
        /*for(i in listaAlumnosNotaFinal) {
            println(i)
        }
         */

        return listaAlumnosNotaFinal
    }


    fun approvedSuspense(lista: MutableList<MutableMap<String, String>>): Pair<MutableList<MutableMap<String, String>>, MutableList<MutableMap<String, String>>> {
        val listaApproved = mutableListOf<MutableMap<String, String>>()
        val listaSuspense = mutableListOf<MutableMap<String, String>>()

        val listaFinal = Pair(listaApproved, listaSuspense)

        for (alumno in lista) {
            val asistencia = alumno["Asistencia"]?.replace("%", "")?.toDouble()
            if (asistencia != null && asistencia < 75) {
                listaSuspense.add(alumno)
            } else if ((alumno["Parcial1"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                if ((alumno["Ordinario1"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                    listaSuspense.add(alumno)
                } else if ((alumno["Parcial2"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                    if ((alumno["Ordinario2"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                        listaSuspense.add(alumno)
                    } else if ((alumno["Practicas"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                        if ((alumno["OrdinarioPracticas"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                            listaSuspense.add(alumno)
                        } else if ((alumno["NotaFinal"]?.toDouble() ?: 0.0) < 5) {
                            listaSuspense.add(alumno)
                        } else {
                            listaApproved.add(alumno)
                        }
                    }
                }
            } else if ((alumno["Parcial2"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                if ((alumno["Ordinario2"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                    listaSuspense.add(alumno)
                } else if ((alumno["Practicas"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                    if ((alumno["OrdinarioPracticas"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                        listaSuspense.add(alumno)
                    } else if ((alumno["NotaFinal"]?.toDouble() ?: 0.0) < 5) {
                        listaSuspense.add(alumno)
                    } else {
                        listaApproved.add(alumno)
                    }
                }
            } else if ((alumno["Practicas"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                if ((alumno["OrdinarioPracticas"]?.replace(",", ".")?.toDouble() ?: 0.0) < 4) {
                    listaSuspense.add(alumno)
                } else if ((alumno["NotaFinal"]?.toDouble() ?: 0.0) < 5) {
                    listaSuspense.add(alumno)
                } else {
                    listaApproved.add(alumno)
                }
            } else if ((alumno["NotaFinal"]?.toDouble() ?: 0.0) < 5) {
                listaSuspense.add(alumno)
            } else {
                    listaApproved.add(alumno)
            }
        }
        return listaFinal
    }

    val lista = approvedSuspense(agregarCalificaciones(getCalificaciones(rutaFichero)))

    println("Aprobados: ${lista.first.count()}")
    for (elemento in lista.first ){
        println(elemento)
    }

    println("\nSuspensos: ${lista.second.count()}")
    for (elemento in lista.second ){
        println(elemento)
    }

}