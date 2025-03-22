package com.fredplugins.kroovy;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

import javax.swing.*;

public class FxTest {
         private static void initAndShowGUI() {
             // This method is invoked on Swing thread
             JFrame frame = new JFrame("FX");
             final JFXPanel fxPanel = new JFXPanel();
             frame.add(fxPanel);
             frame.setVisible(true);

            
         }

         private static void initFX(JFXPanel fxPanel) {
             // This method is invoked on JavaFX thread
             Scene scene = new Scene(new javafx.scene.Group(), 400, 400, Color.BLUE);
             fxPanel.setScene(scene);
         }

         public static void main(String[] args) {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     initAndShowGUI();
                 }
             });
         }
 }