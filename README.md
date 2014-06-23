Classpath Scan Library
======================

Classpath Scan Library is a small library, zero-dependency library used to scan and find resources on the java classpath.  Other libraries have similar functionality, such as [Spring's ClassPathScanningCandidateComponentProvider](http://docs.spring.io/spring/docs/2.5.x/api/org/springframework/context/annotation/ClassPathScanningCandidateComponentProvider.html).  If you are already using Spring then I recommend trying Spring's classpath scanning functionality. There is also the [Reflections Library](https://code.google.com/p/reflections/) which is dedicated to scanning resources on the class path.

So Why This Library?
--------------------

This library exists to provide a small library dedicated to doing one thing well: scanning resources and classes on the classpath without depending on any other library.  This library started because I couldn't use the above two libraries for various reasons.  I was developing some low-level libraries and didn't feel it was wise to mandate using Spring and especially specific Spring versions for such a low level library.  Choosing a framework like Spring is entirely appropriate when developing applications, but not so much for developing libraries (unless the library explicitly exists to enhance or extend Spring).

I had been happily using the  [Reflections Library](https://code.google.com/p/reflections/)  for some time.  However, my team has experienced conflicts due to Reflections' dependency on Google Guava.  There are many versions of Guava in use and there are binary incompatibilities between versions that have bit us many times.  Reflections also just stopped working one day when upgrading a project and I was never able to determine why.  This gave me the itch that was scratched in the form of the Classpath Scan Library.

Limitations
-----------

The main limitation of the current library is that classes are loaded when scanned.  This means that the classes will stay loaded once you have scanned them.  However, the classes are not initialized.  In the future, I will hopefully implement class scanning using ASM to avoid this limitation.  For now, just limit the packages that you scan to the absolute minimum.  Resource scanning does not suffer from this problem.

License
-------

The code is licensed under the [Apache 2 License](LICENSE.txt).



