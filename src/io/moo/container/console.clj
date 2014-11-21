; The MIT License (MIT)
; 
; Copyright (c) 2014 moo.io - Erhan Bagdemir
; 
; Permission is hereby granted, free of charge, to any person obtaining a copy
; of this software and associated documentation files (the "Software"), to deal
; in the Software without restriction, including without limitation the rights
; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
; copies of the Software, and to permit persons to whom the Software is
; furnished to do so, subject to the following conditions:
; 
; The above copyright notice and this permission notice shall be included in
; all copies or substantial portions of the Software.
; 
; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
; THE SOFTWARE.

(ns io.moo.container.console
  (:gen-class)
  (:use io.moo.container.commands)
  (:use io.moo.container.defs)
  (:use [clojure.string :only [split, blank?]])
  (:use io.moo.container.os.stty)
  (:import [io.moo.container.commands CommandTemplate]))

;; Macro definition of infinite loop for REPL. 
(defmacro forever [ & body ]
  `(while true ~@body))

;; Prints message of the day on start-up.
(defn print-motd []
  (print "
           (    )
            (oo)
   )\\.-----/(O O)
  # ;       / u
    (  .   |} )
     |/ `.;|/;     Moo version 0.0.1 [ Type 'help' to get help! ]
     \"     \" \"     https://github.com/mooito/moo

")
  (flush))

(defn split-parameters 
  "Split parameters in form of command and parameters"
  [input]
  (if (not (blank? input))
    (split input #"\s" 2)
    ""))

(defn print-prompt 
  "Prints the command prompt."
  []
  (print "moo> ")
  (flush))

; removes last character from the string.
(defmacro remove-last [ txt ]
  `(subs ~txt 0 (- (count ~txt) 1)))

(defmacro handle-backspace 
  "macro that handles backspace strokes"
  [command-buffer vertical-cursor-pos]
  `(if (not-empty ~command-buffer)
     (do
       (print "\b \b")
       (flush)
       (recur (remove-last ~command-buffer) (dec ~vertical-cursor-pos)))
     (recur ~command-buffer 0)))

(defmacro handle-left
  "Macro that handles left arrow key stroke."
  [command-buffer vertical-cursor-pos]
  `(if (and (< ~vertical-cursor-pos (count ~command-buffer)))
     (do 
       (print (char 27))
       (print (char 91))
       (print (char 67))
       (flush)
       (recur ~command-buffer (inc ~vertical-cursor-pos)))
     (recur ~command-buffer ~vertical-cursor-pos)))

(defmacro handle-right
  "Macro that handles right arrow key stroke."
  [command-buffer vertical-cursor-pos]
  `(if (> ~vertical-cursor-pos 0)
     (do
      (print (char 27))
      (print (char 91))
      (print (char 68))
      (flush)
      (recur ~command-buffer (dec ~vertical-cursor-pos)))
    (recur ~command-buffer ~vertical-cursor-pos)))

(defmacro handle-enter
  "Macro handles enter key stroke."
  [command-buffer input-char]
  `(let [input-token# (split-parameters ~command-buffer)]
     (if 
         (or 
          (blank? ~command-buffer)
          (not= 
           (perform  
            (CommandTemplate.) 
            (first input-token#) 
            (get input-token# 1)) :TERMINATE))
       (do 
         (print (char ~input-char))
         (print-prompt)
         (recur nil 0)))))

(defn clean-command-line 
  "macro that handles backspace strokes"
  [vertical-cursor-pos command-buffer]
  (loop [curr-pos (if (> (count command-buffer) vertical-cursor-pos)  
                         (count command-buffer)
                         vertical-cursor-pos)]
    (if (> curr-pos 0) 
      (do
        (print "\b \b")
        (flush)
        (recur (dec curr-pos))))))
  
(def history-cursor (atom 0))

;; REPL implementation.
(defn repl
  "Read-Eval-Print-Loop implementation"
  []
  (print-prompt)
  (loop [command-buffer nil vertical-cursor-pos 0]
    (let [input-char (.read System/in)]     
      (cond  
       (= input-char ascii-escape)
       (do 
         ;; by-pass the first char after escape-char.
         (.read System/in)
         (let [escape-char (.read System/in) ]
           (cond
            
            (= escape-char ascii-right)
            (handle-right command-buffer vertical-cursor-pos)
            
            (= escape-char ascii-up)            
            (let [command (if (> (count @command-history) 0) (nth @command-history @history-cursor) "") command-size (count command)]
              (if (not-empty @command-history)  
                (do
                  (clean-command-line vertical-cursor-pos command-buffer)
                  (print command)
                  (flush)
                  (if (> @history-cursor 0)
                    (swap! history-cursor dec)
                    (reset! history-cursor (dec (count @command-history))  ))))
              (recur command (dec command-size)))
            
            (= escape-char ascii-down)
            (let [command (if (> (count @command-history) 0) (nth @command-history @history-cursor) "") command-size (count command)]                   
              (if (not-empty @command-history)
                (do
                  (clean-command-line vertical-cursor-pos command-buffer)
                  (print (nth @command-history @history-cursor))
                  (flush)
                  (if (= @history-cursor (dec (count @command-history)))
                    (reset! history-cursor 0)
                    (reset! history-cursor (inc @history-cursor))
                    )))
              (recur command (dec command-size)))
            
            (= escape-char ascii-left)
            (handle-left command-buffer vertical-cursor-pos))))
       
       ;; on enter pressed.
       (= input-char ascii-enter)
       (do
         (reset! history-cursor 0)
         (handle-enter command-buffer input-char))
       ;; on backspace entered.
       (= input-char ascii-backspace)
       (handle-backspace command-buffer vertical-cursor-pos)
       ;; default case
       :else
       (do
         (print (char input-char))
         (flush)
         (recur (str command-buffer (char input-char)) (inc vertical-cursor-pos)))))))