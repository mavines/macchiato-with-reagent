An example website built with Shadow-cljs, Macchiato, and Reagent.

# Setup
The repository includes a Vagrantfile for quickly setting up your environment. If you have Oracle's VirtualBox installed, simply:
```bash
vagrant up
```
and you should be ready to go. The VM includes Emacs with Prelude installed.

# Develop

Build the server with hot reloading:
```bash
shadow-cljs watch server
```

Build the client with hot reloading
```bash
shadow-cljs watch client
```

Run the server:
```
sudo node target/main.js
```

Once it is running, view the page at ```localhost:80```. If you are running from the virtual machine, use ```localhost:8080```

## REPL

Connect to the server side repl:
```bash
shadow-cljs cljs-repl server
```

Connect to the client repl:
```
shadow-cljs cljs-repl client
```

Connect to client or server with CIDER in emacs.
Port listed when starting the `watch` build.

``` bash
cider-connect-cljs
```
select **shadow** and then ```:server``` or ```:client```

# References
Most of the client code is taken from the [Reagent Example](http://reagent-project.github.io/)  
The server is using [Macchiato](https://macchiato-framework.github.io/)  
Emacs is configured with [Prelude](https://github.com/bbatsov/prelude)
