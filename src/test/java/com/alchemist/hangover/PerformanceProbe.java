package com.alchemist.hangover;

import com.alchemist.hangover.database.*;
import com.alchemist.hangover.model.*;
import com.alchemist.hangover.view.chapter.*;
import javax.swing.*;
import java.awt.Image;
/** Misure manuali senza schermo: costruzione del pannello, stanze, icone e H2 in memoria.
 * Non rappresentano il tempo di una partita completa o della finestra visibile. */
public class PerformanceProbe {
 static void timed(String label, Runnable r) { long t=System.nanoTime(); r.run(); System.out.printf("BENCH %s %.2f ms%n",label,(System.nanoTime()-t)/1e6); }
 public static void main(String[] args) throws Exception {
  System.setProperty(DatabaseManager.TEST_DATABASE_PROPERTY,"jdbc:h2:mem:perf;DB_CLOSE_DELAY=-1");
  timed("H2 init memoria", DatabaseManager::initDatabase);
  SwingUtilities.invokeAndWait(()->{
   final Chapter1Panel[] p=new Chapter1Panel[1];
   timed("costruzione cap1 senza finestra",()->p[0]=new Chapter1Panel(null));
   for(int i=0;i<3;i++)timed("giro 7 stanze cap1",()->{for(int n=1;n<=7;n++)p[0].loadRoom(n);});
   for(int i=0;i<3;i++)timed("20 icone inventario 155px",()->{for(int n=0;n<20;n++)com.alchemist.hangover.view.ImageAssets.scaled("/images/chapter1/items/map.png",155,155);});
   for(int i=0;i<3;i++)timed("salvataggio H2 memoria",()->SaveGameDAO.saveGame(1,"benchmark"));
  });
  DatabaseManager.close();
 }
}
