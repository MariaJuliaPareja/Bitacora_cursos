package com.example.bitacora_cursos;

public class Clases {
    String claseid, seccion, area, tema;
    public Clases(String claseid, String seccion, String area, String tema){
        this.claseid = claseid;
        this.seccion = seccion;
        this.area = area;
        this.tema = tema; }
    public String getClaseid() {
        return claseid; }

    public String getSeccion() { return seccion; }

    public String getArea() { return area; }
    public String getTema() { return tema; }
    public void setTema(String tema) {this.tema = tema;}

}
