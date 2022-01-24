package nao_package;

import javax.swing.text.*;

/**
 * NaoDocument
 * JTextFieldなどで文字数制限や数字のみの入力などをする PlainDocumentクラス
 *
 * @author takaesu
 */

public class NaoDocument extends PlainDocument
{
    String validValues = "0123456789.+-";   // 入力可能文字
    int    limit       = 100;   // 入力可能文字数

    // コンストラクタ
    public NaoDocument( String validValues, int limit ){
        this.validValues = validValues;
        this.limit = limit;
    }
/***
    NaoDocument( String chars ){
        this( validValues, limit );
    }
    NaoDocument( int limit ){
        this( chars, limit );
    }
    NaoDocument(){
        this( validValues, limit );
    }
***/

    //-- メソッド
    public void insertString( int offset, String str, AttributeSet a ){
        // 入力文字のチェック
        if( validValues.indexOf(str) == -1 ){
            return;
        }

        // 文字数のチェック
        if( getLength() >= limit ){
            return;
        }

        // 
        try{
            super.insertString(offset, str, a);
        } catch( BadLocationException e ){
            System.out.println(e);
        }
    }
}
