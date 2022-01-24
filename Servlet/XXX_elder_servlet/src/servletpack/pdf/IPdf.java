package servletpack.pdf;

public interface IPdf {
    public void setParameter(String paramname, Object o);
    public Object getParameter(String paramname);
    public int VrSetForm(String formname, int n);
    public int VrsOut(String field, String data);
    public void addRecordField(final String[] fields);
    public int setRecordString(String field, int gyo, String data);
    public int VrsOutn(String field, int gyo, String data);
    public int VrImageOut(String field, String filePath);
    public int VrAttribute(String field, String attr);
    public int VrAttributen(String field, int gyo, String attr);
    public int VrEndRecord();
    public int VrEndPage();
    public int close(boolean hasData);
    public int close();
}
