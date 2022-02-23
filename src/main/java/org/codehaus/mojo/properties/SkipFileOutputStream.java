package org.codehaus.mojo.properties;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SkipFileOutputStream extends FilterOutputStream {

    private boolean filter = true;
    private boolean headerLineJustEnded = false;
    private StringBuilder sb = new StringBuilder();

    public SkipFileOutputStream(final OutputStream out) {
        super(out);
    }

    @Override
    public void write(final int b) throws IOException {
        if (filter) {
            switch (b) {
                case '\n':
                case '\r':
                    headerLineJustEnded = true;
                    sb.setLength(0);
                    break;
                case '#':
                    if (headerLineJustEnded) {
                        headerLineJustEnded = false;
                        sb.setLength(0);
                    }
                case ' ':
                case '\t':
                    sb.append(b);
                    break;
                default:
                    if (headerLineJustEnded) {
                        filter = false;
                        super.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                        sb.setLength(0);
                        super.write(b);
                    }
            }
        } else {
            super.write(b);
        }
    }
}

