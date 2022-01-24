<?php

require_once('for_php7.php');

class knjxmenuChg_pwd
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]  = $objForm->get_start("login", "POST", "index.php", "", "login");

        if ($model->menuEnglish != 1) {
            $arg["MENUNAME"] = "パスワード変更";
        } else {
            $arg["MENUNAME"] = "Change Password";
        }
        
        //右側の固定文字も英語対応にする
        if ($model->menuEnglish != 1) {
            $arg["LOGOUT"] = "ログアウト";
            $arg["THOLD"] = "旧パスワード";
            $arg["THNEW"] = "新パスワード";
            $arg["THAGAIN"] = "確認";
        } else {
            $arg["LOGOUT"] = "Logout";
            $arg["THOLD"] = "Old Password";
            $arg["THNEW"] = "New Password";
            $arg["THAGAIN"] = "Retype New Password";
        }
       
        //fontsizeによってcssのクラス変える
        if ($model->size != 1) {
            $arg["csssize"] = "big";
        } else {
            $arg["csssize"] = "";
        }

        $db = Query::dbCheckOut();
        //学校名表示
        $gakuQuery = knjxmenuQuery::getGaku($model);
        $gakuRow = $db->getOne($gakuQuery);
        $arg["GAKU_NAME"] = $gakuRow;
        
        Query::dbCheckIn($db);
        

        if ($model->menuEnglish != 1) {
            $arg["CTRL_YEAR"] = CTRL_YEAR ."年度&nbsp;" .CTRL_SEMESTERNAME ."　処理日付：" .CTRL_DATE;
        } else {
            $arg["CTRL_YEAR"] = CTRL_YEAR ."&nbsp;&nbsp;" .CTRL_SEMESTERNAME ."　DATE：" .CTRL_DATE;
        }
        $arg["STAFFNAME_SHOW"] = STAFFNAME_SHOW;
        $arg["REQUESTROOT"] = REQUESTROOT;


        $auth = $_SESSION["auth"];
        $userid = $auth->auth["USERID"];
        $pass   = $auth->auth["PASSWD"];
        $challenge = $_SESSION["challenge"];
        //旧パスワード
        $objForm->ae(array("type"       => "text",
                            "name"       => "password",
                            "size"       => 17,
                            "maxlength"  => 32,
                            "extrahtml"  => "onblur=\"moji_hantei(this);\" STYLE=\"ime-mode:disabled; margin-bottom:0px; font-size:100%;\"",
                            "pass"       => "true",
                            "value"       => "" ));

        $arg["password"] = $objForm->ge("password");

        //新パスワード１
        $objForm->ae(array("type"       => "text",
                            "name"       => "password1",
                            "size"       => 17,
                            "maxlength"  => 32,
                            "extrahtml"  => "onblur=\"moji_hantei(this);\" STYLE=\"ime-mode:disabled; margin-bottom:0px; font-size:100%;\"",
                            "pass"       => "true",
                            "value"       => "" ));

        $arg["password1"] = $objForm->ge("password1");

        //新パスワード２
        $objForm->ae(array("type"       => "text",
                            "name"       => "password2",
                            "size"       => 17,
                            "maxlength"  => 32,
                            "extrahtml"  => "onblur=\"moji_hantei(this);\" STYLE=\"ime-mode:disabled; margin-bottom:0px; font-size:100%;\"",
                            "pass"       => "true",
                            "value"       => "" ));

        $arg["password2"] = $objForm->ge("password2");

        //更新ボタンを作成する
        if ($model->menuEnglish != 1) {
            $val = "更 新";
        } else {
            $val = "O K";
        }
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => $val,
                            "extrahtml"   => "onclick=\"return doChallengeResponse();\""));

        $arg["btn_update"] = $objForm->ge("btn_udpate");

        //リセットボタンを作成する
        if ($model->menuEnglish != 1) {
            $val = "リセット";
        } else {
            $val = "Reset";
        }
        $objForm->ae(array("type" => "reset",
                            "name"        => "btn_reset",
                            "value"       => $val,
                            "extrahtml"   => "" ));

        $arg["btn_reset"] = $objForm->ge("btn_reset");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "response"));

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "challenge",
                            "value"     => $challenge));

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "username",
                            "value"     => $userid));

        $cssplugin = "kenjaMenu{$model->cssNo}.css|kyotu.css|css/font-awesome.min.css";



        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML4($model, "knjxmenuChg_pwd.html", $arg, "", $cssplugin);
        
        
        echo "<script language=\"JavaScript\">\n";
        //echo "window.open('index.php?cmd=retree','left_frame');\n";
        echo "</script>";
    }
}
