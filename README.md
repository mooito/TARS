TARS
===

<img src="https://travis-ci.org/mooito/TARS.svg" /> [![API Documentation](http://b.repl.ca/v1/doc-API-blue.png)](http://www.ryos.io/tars/doc/)
<img src="https://img.shields.io/packagist/l/doctrine/orm.svg"/>
[![GitHub version](https://badge.fury.io/gh/mooito%2FTARS.svg)](https://badge.fury.io/gh/mooito%2FTARS)
[![Clojars Project](https://img.shields.io/clojars/v/io.ryos/tars.svg)](https://clojars.org/io.ryos/tars)

TARS is a Clojure framework, that provides a command-line interface for your applications and allows your users to interact through it (like mongo, mysql clients). TARS already understands a few commands like "help" and "quit". You only need to extend it to make TARS understand your custom commands specific to your clients.

+ [API Doc](http://www.ryos.io/tars/doc/)
+ [GitHub Issues](https://github.com/ryos-io/tars/issues)


How to use
---

To add the CLI into your application just add the dependency and the define the main function.

```
(defproject your-app "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [io.ryos/tars "0.1.5"]]
  :main io.ryos.tars.container)
```

After you run your application by calling:
```
lein run
```
the CLI will be available for user interaction with a default MOTD and prompt. You can override this settings and customize them in your applications.

```
        .
       _|_
/\/\  (. .)
`||'   |#|
 ||__.-"-"-.___
 `---| . . |--.\     TARS version 0.1.0 [ Type 'help' to get help! ]
     | : : |  |_|    https://github.com/ryos-io/tars
     `..-..' ( I )
      || ||   | |
      || ||   |_|
     |__|__|  (.)
tars>
```
Out of the box, TARS provide two commands, that are "help" and "quit". You can now extend the TARS to understand your commands.


How to customize
---

You can override the MOTD by creating a new branding file under "~/.tars/branding" and also the prompt by adding a configuration file in "~/.tars/config.clj". The configuration file will be loaded while the CLI starts. To override the prompt settings, just add a new definition for the prompt:

```
(def config {:prompt "tars"})
```
How to extend
---
To add your own commands you can use the TARS DSL:

```
(ns test-prj.core
  (:gen-class)
  (:require [io.ryos.tars.container :as c])
  (:use io.ryos.tars.dsl))

;; add a new command called "test"
(add-command "test"
 (on-start (println "starting"))
 (on-exec  (println "exec"))
 (on-complete (println "complete"))
 (on-error (println "error"))
 (with-doc "Command Description"))

(defn -main [ & args ]
    (c/start-repl))
```

## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/ryos-io/tars/issues).

 
## LICENSE

Copyright 2015 Erhan Bagdemir under MIT License.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
