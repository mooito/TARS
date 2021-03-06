; The MIT License (MIT)
;
; Copyright (c) 2014 ryos.io - Erhan Bagdemir
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

(ns io.ryos.tars.commands
  (:gen-class)
  (:require [io.ryos.tars.rendering :as r]))

;; command history size
(def command-history-size 5)

;; command history
(def command-history (atom '()))

(defn put-command-to-history
  "Put a new command item to the history. Deletes the last item if the history exceeds its max. size"
  [command]
  (swap! command-history conj command)
  (if (> (count @command-history) command-history-size)
    (swap! command-history drop-last)))

(def command-map
  { "quit" {:console-action :TERMINATE, :desc "\nType 'quit' to exit the console."},
    "help" {:console-action :CONTINUE,  :desc "\nType 'help' or 'help <command>' to get help."}
    "moo"  {:console-action :CONTINUE,  :desc "\nJust moo!"}})

(def help-data (atom command-map))

(defn add-command-doc [command-name doc]
  (reset! help-data (assoc command-map command-name {:console-action :CONTINUE, :desc doc})))

(defprotocol Command
  (perform [self, command, param]
    "Executes the command logic."))

(def identity-func (fn [x] x))

;; Console command on-start action
(defmulti on-start identity-func)
(defmulti on-error identity-func)
(defmulti on-complete identity-func)
(defmulti exec (fn [command param] command))

;; 'quit' command implementation.
(defmethod on-start "quit" [ params ])
(defmethod on-error "quit" [ params ]
  (r/prints println "quit failed!"))
(defmethod exec "quit" [ commands params ])
(defmethod on-complete "quit" [ params ]
  (r/prints print "\nBye!\n")
  (:console-action (get @help-data "quit")))

;; 'help' command implementation
(defmethod on-start "help" [ params ])
(defmethod on-error "help" [ params ]
  (println "help failed!"))
(defmethod exec "help" [ command params ]
  (if (clojure.string/blank? params)
    (println (str "\nPlease provide a command to get help: e.g 'help quit'"))
  (let [desc (:desc (get @help-data params))]
    (if (clojure.string/blank? desc)
      (r/prints println (str "\nHelp not found for: '" params "'"))
      (r/prints println desc)))))
(defmethod on-complete "help" [ params ]
  (:console-action (get @help-data "help")))

;; default command implementations
(defmethod exec :default [ command params ])
(defmethod on-start :default [ params ])
(defmethod on-error :default [ params ])
(defmethod on-complete :default [ command ]
  (r/prints println (str "\nUnknown command '" command "'. Type 'help' to get help.")))

(deftype CommandTemplate []
  Command
  (perform [self, command, params]
    (try
      (on-start command)
      (exec command params)
      (put-command-to-history (str command " " params))
      (catch Exception e (on-error command e)))
    (on-complete command)))
