package aed;
import java.util.ArrayList;
import java.util.List;

public class ColaPrioridad<T extends Comparable<T>>{
    private ArrayList<Nodo> heapList;
    private ArrayList<Nodo> apartados;

    private class Nodo {
        //creamos una clase anidada Nodo que contiene información de posición y estado de cada elemento
        private T valor;
        private int heapIndex;
        private boolean apartado;

        public Nodo(T v, int index){
            valor = v;
            heapIndex = index;
            apartado = false;
        }
    }

    public ColaPrioridad() {
        //O(1) contractor "normal"
        heapList = new ArrayList<Nodo>();
        apartados = new ArrayList<Nodo>();
    }

    public ColaPrioridad(List<T> array){
        //O(n) contrucor por copia usa el algoritmo de floyd para tener array2heap en O(n)
        if (array.size() == 0) return;

        heapList = new ArrayList<Nodo>();
        apartados = new ArrayList<Nodo>();

        //paso tal como está el array
        for (int i = 0; i< array.size(); i++){
            Nodo nuevo = new Nodo(array.get(i), i);
            heapList.add(nuevo);
        }

        if (array.size() <= 3){
            bajar(0);
            return;
        }

        //buscamos el index del ultimo nodo en la penultima altura aprovechando que es un arbol casi perfecto
        int cantNodosPorNivel = 1 ;
        int IndexUltimoDeNivel = 1;
        while (IndexUltimoDeNivel <= array.size()){
            IndexUltimoDeNivel = IndexUltimoDeNivel + cantNodosPorNivel *2;
            cantNodosPorNivel *=2;
        }
        IndexUltimoDeNivel = IndexUltimoDeNivel - cantNodosPorNivel -1;

        //usamos hepify desde el penúltimo nivel hacia arriba
        for (int i = IndexUltimoDeNivel; i >= 0; i--){
            bajar(i);
        }
    }

    public class HandleHeap{
        //permite manipular un nodo específico, sin tener que buscarlo y son romper la encapsulación
        private Nodo nodoReference;


        private HandleHeap(Nodo objet){
            nodoReference = objet;
        }

        public T valor (){
            return nodoReference.valor;
        }
        public T eliminar(){
            return ColaPrioridad.this.eliminar(nodoReference.heapIndex);
        }

        public void modificar (T elem){
            //cambia el valor del nodo sin cambiar nada más
            nodoReference.valor = elem;

            if (!nodoReference.apartado) ordenar(nodoReference.heapIndex);
        }

        public void integrar(){
            // O(log(n)) si un nodo estaba apartado, lo integra al heap principal, lo tiene que ordenar
            if (!nodoReference.apartado) return;

            //eliminamos el nodo de apartados en O(1)
            eliminarRapido(apartados, nodoReference.heapIndex);
            nodoReference.apartado = false;

            //agregamos el nodo a heap
            nodoReference.heapIndex = cardinal();
            heapList.add(nodoReference);
            ordenar(cardinal()-1);
        }

    }

    public HandleHeap encolar(T elem){
        //O(log(n)), dado un valor, crea un nodo y lo acomoda en el heap
        Nodo nuevo = new Nodo(elem, heapList.size());
        heapList.add(nuevo);
        ordenar(cardinal()-1);
        return new HandleHeap(nuevo);
    }

    public T desencolar (){
        //O(log(n)) elimina del heap y retorna el valor del nodo con más prioridad del heap (el primero)
        return eliminar(0);
    }

    public HandleHeap apartar (){
        // O(log(n)) eliminamos del heap y agregamos a apartados (lo saca del heap principal, pero mantiene una referencia para que si tenía un handle no pierda validez)
        Nodo apartado = heapList.get(0);
        apartado.apartado = true;
        apartados.add(apartado);
        eliminar(apartado.heapIndex);
        apartado.heapIndex = apartados.size()-1;
        //entregamos una copia handle para manejar el elemento
        return new HandleHeap(apartado);
    }

    public int cardinal(){
        //O(1), retorna la cantidad de elementos en el heap
        return heapList.size();
    }

    private T eliminar (int index){
        // O(log(n)) usa eliminar rapido con O(1) y ordenar con O(log(n))

        Nodo eliminado = heapList.get(index);

        if (eliminarRapido(heapList, index) != -1) {
            ordenar(index);
        }

        return eliminado.valor;
    }

    private void ordenar(int index){
        //O(log(n)) dado el indice de un elemento, verifica si tiene que subir o bajar en el heap
        //solo ejecutará el que necesite
        if (heapList.size() == 0) return;

        subir(index);
        bajar(index);
    }

    private void subir(int index) {
        //O(log(n)), compara el valor con el padre y sigue subiendo hasta estar bien ubicado

        // Mientras no sea la raíz
        while (index > 0) {

            Nodo target = heapList.get(index);
            Nodo padre = heapList.get((index - 1) / 2);

            if (target.valor.compareTo(padre.valor) < 0) {
                intercambiar(target, padre);
                index = target.heapIndex;
            }
            else return;
        }
    }

    private void bajar (int index){
        // O(log(N)), compara el valor con sus hijos (los casos que los tenga) y baja hasta que este en el lugar correcto

        Nodo target = heapList.get(index);

        while(true){
            //tiene dos hijos
            if (cardinal()>2*index+1 && cardinal()>2*index+2){
                Nodo izquierdo = heapList.get(2*index+1);
                Nodo derecho = heapList.get(2*index+2);

                //si no es mas grande que alugno de sus hijos
                if (target.valor.compareTo(izquierdo.valor) < 0 && target.valor.compareTo(derecho.valor) < 0){
                    return;
                }

                //si el hijo derecho es mas chico
                if (izquierdo.valor.compareTo(derecho.valor) > 0){
                    intercambiar(target, derecho);
                }//si el izquierdo es mas chico
                else{
                    intercambiar(target, izquierdo);
                }
            }

            //solo hijo izquierdo
            else if (cardinal()>2*index+1){
                Nodo izquierdo = heapList.get(2*index+1);
                if (target.valor.compareTo(izquierdo.valor) > 0 ){
                    intercambiar(target, izquierdo);
                }
                else return; //no necesita bajar
            }

            else return;//es hoja

            index = target.heapIndex;
        }
    }

    private void intercambiar (Nodo a, Nodo b){
        // O(1), dado dos nodos los intercambia tomando en cuenta su index y valor
        // lo intercambiamos
        heapList.set(b.heapIndex, a);
        heapList.set(a.heapIndex, b);

        //actualizamos su index
        int temp = a.heapIndex; // primitive type (sin antialiasing)
        a.heapIndex = b.heapIndex;
        b.heapIndex = temp;
    }

    private int eliminarRapido(ArrayList<Nodo> lista, int index) {
        // O(log(n)), siempre eliminia el ultimo, devuelve el index del elemento que movió si movió alguno, si no -1

        //si es el ultimo elemen
        if (index == lista.size()-1){
            lista.remove(index); //ultimo elemento O(1)
            return -1;
        }

        //sobreescribe el último elemento en el luegar del eliminado
        Nodo ultimo = lista.get(lista.size()-1);
        ultimo.heapIndex = index;
        lista.set(index, ultimo);
        lista.remove(lista.size()-1);
        return index;
    }

    @Override
    public String toString(){
        //O(n) convierte la lista del heap en una string, usando to string del valor (elemento genérico)
        if (cardinal() == 0) return "[]";

        String res = "[";
        for (int i = 0; i < cardinal()-1; i++){
            res += heapList.get(i).valor + ", ";
        }

        res += heapList.get(cardinal()-1).valor + "]";
        return res;

    }

    //debug use only
    public String toStringValor(){
        // imprime los índices de cada elemento, siempre debe devolver una lista ordenada descendente empezando desde 0.
        if (cardinal() == 0) return "[]";

        String res = "[";
        for (int i = 0; i < cardinal()-1; i++){
            res += heapList.get(i).heapIndex + ", ";
        }

        res += heapList.get(cardinal()-1).heapIndex + "]";
        return res;
    }
}