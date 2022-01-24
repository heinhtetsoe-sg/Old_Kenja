# $Id: readme.txt,v 1.4 2017/07/10 11:37:23 m-yama Exp $

2017/07/10  1.新規作成
            2.common.php(金丸さん)
            -- function SecurityCheck($staffcd, $appname)
            -- のfunctionの中で
            -- $query = "VALUES security_chk_prg('" .$staffcd ."','" .$appname ."','" .CTRL_YEAR  ."','" . SCHOOLKIND ."','" . SCHOOLCD ."')";
            -- と書かれていたので、useSchool_KindMenuを参照し、分岐を作成しました。
            3.controller.php(金丸さん)
            -- function session_start()
            -- のfunctionの最後のところでSQLを発行する際に
            -- $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
            -- $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            -- のふたつの条件はuseSchool_KindMenuを参照し値が1の時のみ追加されるように修正しました。
            4.localPHPlib.php(金丸さん)
            -- function auth_validatelogin()
            -- の中ほどでSQLを発行する際に
            -- $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
            -- $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            -- のふたつの条件はuseSchool_KindMenuを参照し値が1の時のみ追加されるように修正しました。
