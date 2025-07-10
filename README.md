# UCB CS61B(L) Summer 2025

[Official site](https://cs61bl.org/su25)

## Notable differences from course guidelines

- Using [Summer 2025](https://cs61bl.org/su25/) material:
  the [official](https://cs61bl.org/su25/policies/#auditing-61bl) [policies](https://sp25.datastructur.es/policies/#auditing-cs61b)
  recommend using the [Spring 2021](https://sp21.datastructur.es/) material
  to utilize the public auto-grader;
  I made the trade-off to follow the latest course work.  
  Note that Homeworks need to be accessed via Gradescope, and thus I just supplement with SP21 ones.
- Partially using [VS Code](https://code.visualstudio.com/):
  the course [highly recommends](https://cs61bl.org/su25/labs/lab01/#intellij-setup:~:text=We%20highly%20recommend%20using%20IntelliJ.) using [JetBrains IntelliJ IDEA](https://www.jetbrains.com/help/idea/getting-started.html) [Community edition](https://www.jetbrains.com/products/compare/?product=idea&product=idea-ce)
  with [Git](https://git-scm.com/doc) CLI;
  I use the Ultimate edition via GitHub Student Developer Pack, 
  utilizing the Git extension, as well as the VS Code editor with the [GitLens](https://marketplace.visualstudio.com/items?itemName=eamodio.gitlens) extension (by [GitKraken](https://www.gitkraken.com/gitlens)).
- Cherry-picking past projects:
  What attracted me to this course was [the gitlet project](https://sp21.datastructur.es/materials/proj/proj2/proj2),
  which is being replaced starting from [sp24](https://sp24.datastructur.es/);
  [su24](https://cs61bl.org/su24/projects/gitlet/design) is the latest version to still have it.
  I also cherry-picked various versions of Project 0.
  - Note that the [gitlet](./gitlet/) tests [would issue warnings with Java 17+](https://openjdk.org/jeps/411) 
    and [wouldn't compile with Java 24+](https://openjdk.org/jeps/486),
    as the testing requires `SecurityManager` to trap system exits.
    I don't about a better way to handle this, so I use Java 21 only for this project.
  - [`git@e83c516`](https://github.com/git/git/tree/e83c5163316f89bfbde7d9ab23ca2e25604af290), 
    the first commit Linus ported to GitHub, is included as a submodule for reference in the gitlet project.
