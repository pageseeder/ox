/*
 * This file is part of the DiffX library.
 *
 * For licensing information please see the file license.txt included in the release.
 * A copy of this licence can also be found at
 *   http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pageseeder.ox.diffx.util;

import org.pageseeder.diffx.algorithm.*;
import org.pageseeder.diffx.event.AttributeEvent;
import org.pageseeder.diffx.event.DiffXEvent;
import org.pageseeder.diffx.format.DiffXFormatter;
import org.pageseeder.diffx.sequence.EventSequence;

import java.io.IOException;

/**
 * Performs the diff comparison using the LCS algorithm.
 *
 * @author Christophe Lauret
 * @version 30 October 2013
 */
public final class DiffXBasic extends DiffXAlgorithmBase {

  // state variables ----------------------------------------------------------------------------

  /**
   * Matrix storing the paths.
   */
  private transient Matrix matrix;

  /**
   * The state of the elements.
   */
  private transient ElementState estate = new ElementState();

  // constructor --------------------------------------------------------------------------------

  /**
   * Creates a new DiffXAlgorithmBase.
   *
   * @param seq0 The first sequence to compare.
   * @param seq1 The second sequence to compare.
   */
  public DiffXBasic(EventSequence seq0, EventSequence seq1) {
    super(seq0, seq1);
    this.matrix = setupMatrix(seq0, seq1);
  }

  // methods ------------------------------------------------------------------------------------

  /**
   * Returns the length of the longest common sequence.
   *
   * @return the length of the longest common sequence.
   */
  @Override
  public int length() {
    // case when one of the sequences is empty
    if (this.length1 == 0 || this.length2 == 0) {
      this.length = 0;
    }
    // normal case
    if (this.length < 0) {
      this.matrix.setup(this.length1 + 1, this.length2 + 1);
      // allocate storage for array L;
      for (int i = super.length1; i >= 0; i--) {
        for (int j = super.length2; j >= 0; j--) {
          // we reach the end of the sequence (fill with 0)
          if (i >= super.length1 || j >= super.length2) {
            this.matrix.set(i, j, 0);
          } else {
            // the events are the same
            if (this.sequence1.getEvent(i).equals(this.sequence2.getEvent(j))) {
              this.matrix.incrementPathBy(i, j, maxWeight(this.sequence1.getEvent(i), this.sequence2.getEvent(j)));
              // different events
            } else {
              this.matrix.incrementByMaxPath(i, j);
            }
          }
        }
      }
      this.length = this.matrix.get(0, 0);
    }
    return this.length;
  }

  /**
   * Writes the diff sequence using the specified formatter.
   *
   * @param formatter The formatter that will handle the output.
   *
   * @throws IOException If thrown by the formatter.
   */
  @Override
  public void process(DiffXFormatter formatter) throws IOException {
    // handle the case when one of the two sequences is empty
    processEmpty(formatter);
    if (this.length1 == 0 || this.length2 == 0) return;
    // calculate the LCS length to fill the matrix
    length();
    int i = 0;
    int j = 0;
    DiffXEvent e1 = this.sequence1.getEvent(i);
    DiffXEvent e2 = this.sequence2.getEvent(j);
    // start walking the matrix
    while (i < super.length1 && j < super.length2) {
      e1 = this.sequence1.getEvent(i);
      e2 = this.sequence2.getEvent(j);
      // both elements are considered equal
      if (e1.equals(e2)) {
        // if we can format checking at the stack, let's do it
        if (this.estate.okFormat(e1)) {
          formatter.format(e1);
          this.estate.format(e1);
          i++;
          j++;

          // otherwise maybe we should insert.
        } else if (this.estate.okInsert(e1)) {
          formatter.insert(e1);
          this.estate.insert(e1);
          i++;

          // or delete.
        } else if (this.estate.okDelete(e2)) {
          formatter.delete(e2);
          this.estate.delete(e2);
          j++;
        } else {
          break;
        }

        // we can only insert or delete, priority to insert
      } else if (this.matrix.isGreaterX(i, j)) {
        // follow the natural path and insert
        if (this.estate.okInsert(e1)) {
          formatter.insert(e1);
          this.estate.insert(e1);
          i++;

          // go counter current and delete
        } else if (this.estate.okDelete(e2)) {
          formatter.delete(e2);
          this.estate.delete(e2);
          j++;
        } else {
          break;
        }

        // we can only insert or delete, priority to delete
      } else if (this.matrix.isGreaterY(i, j)) {
        // follow the natural and delete
        if (this.estate.okDelete(e2)) {
          formatter.delete(e2);
          this.estate.delete(e2);
          j++;

          // insert (counter-current)
        } else if (this.estate.okInsert(e1)) {
          formatter.insert(e1);
          this.estate.insert(e1);
          i++;
        } else {
          break;
        }

        // elements from i inserted and j deleted
        // we have to make a choice for where we are going
      } else if (this.matrix.isSameXY(i, j)) {
        // we can insert the closing tag
        if (this.estate.okInsert(e1)
            && !(e2 instanceof AttributeEvent && !(e1 instanceof AttributeEvent))) {
          this.estate.insert(e1);
          formatter.insert(e1);
          i++;

          // we can delete the closing tag
        } else if (this.estate.okDelete(e2)
            && !(e1 instanceof AttributeEvent && !(e2 instanceof AttributeEvent))) {
          formatter.delete(e2);
          this.estate.delete(e2);
          j++;

        } else {
          break;
        }
      } else {
        break;
      }
    }

    // finish off the events from the first sequence
    while (i < super.length1) {
      this.estate.insert(this.sequence1.getEvent(i));
      formatter.insert(this.sequence1.getEvent(i));
      i++;
    }
    // finish off the events from the second sequence
    while (j < super.length2) {
      this.estate.delete(this.sequence2.getEvent(j));
      formatter.delete(this.sequence2.getEvent(j));
      j++;
    }
    // free some resources
    //    matrix.release();
  }

  // private helpers (probably inlined by the compiler) -----------------------------------

  /**
   * Writes the diff sequence using the specified formatter when one of
   * the sequences is empty.
   *
   * <p>The result becomes either only insertions (when the second sequence is
   * empty) or deletions (when the first sequence is empty).
   *
   * @param formatter The formatter that will handle the output.
   *
   * @throws IOException If thrown by the formatter.
   */
  private void processEmpty(DiffXFormatter formatter) throws IOException {
    // the first sequence is empty, events from the second sequence have been deleted
    if (this.length1 == 0) {
      for (int i = 0; i < this.length2; i++) {
        formatter.delete(this.sequence2.getEvent(i));
      }
    }
    // the second sequence is empty, events from the first sequence have been inserted
    if (this.length2 == 0) {
      for (int i = 0; i < this.length1; i++) {
        formatter.insert(this.sequence1.getEvent(i));
      }
    }
  }

  /**
   * Determines the most appropriate matrix to use.
   *
   * <p>Calculates the maximum length of the shortest weighted path if both sequences
   * are totally different, which corresponds to the sum of all the events.
   *
   * @param s1 The first sequence.
   * @param s2 The second sequence.
   *
   * @return The most appropriate matrix.
   */
  private static Matrix setupMatrix(EventSequence s1, EventSequence s2) {
    int max = 0;
    for (int i = 0; i < s1.size(); i++) {
      max += s1.getEvent(i).getWeight();
    }
    for (int i = 0; i < s2.size(); i++) {
      max += s2.getEvent(i).getWeight();
    }
    if (max > Short.MAX_VALUE) return new MatrixInt();
    else return new MatrixShort();
  }

  /**
   * Returns the max weight of the two events.
   *
   * @param e1 The first event.
   * @param e2 The second event.
   *
   * @return The weight for the event.
   */
  private int maxWeight(DiffXEvent e1, DiffXEvent e2) {
    return e1.getWeight() > e2.getWeight() ? e1.getWeight() : e2.getWeight();
  }

}
