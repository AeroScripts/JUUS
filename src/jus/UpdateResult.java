/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jus;

import java.io.IOException;

/**
 *
 * @author Aero
 */
public interface UpdateResult {
    public void finished(long time);
    public void failed(long time, Exception exception);
}
