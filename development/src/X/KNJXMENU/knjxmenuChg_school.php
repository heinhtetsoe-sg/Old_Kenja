<?php

require_once('for_php7.php');

class knjxmenuChg_school
{
    public function main(&$model)
    {
        $menu = $_POST["menu"];
        if (strlen(trim($menu))>0) {
            echo "<html><head>";
            echo "<script type=\"text/javascript\">";
            echo "<!--";
            echo "　　window.close();";
            echo "//-->";
            echo "</script>";
            echo "</head>";
            echo "<body>";
            echo "キャンセルいたしました。";
            echo "</body>";
            echo "</html>";
            return false;
        }
        $objForm = new form();
        //フォーム作成
        $arg["start"]  = $objForm->get_start("login", "POST", "index.php", "", "login");

        $auth = $_SESSION["auth"];
        $userid = $auth->auth["USERID"];
        $pass   = $auth->auth["PASSWD"];
        $challenge = $_SESSION["challenge"];

        $db = Query::dbCheckOut();

        //切替先学校選択
        $opt = array();
        $opt[] = array('label' => "以下から切替先学校を選択してください",
                       'value' => 0);
        $value_flg = false;
        $query = knjxmenuQuery::getChangeSchool();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "";
        $arg["menu"] = knjCreateCombo($objForm, "menu", $value, $opt, $extra, 3);

        //echo "{"."$menu"."}<hr>";

        //学校切替ボタンを作成する
        $extra = "onclick='return doChangeSchool();'";
        $arg["btn_change"] = knjCreateBtn($objForm, "btn_change", "学校切替", $extra);

        //v(^|^)_↓削除する
        //閉じるボタンを作成する
        //$extra = "onclick='return doClose();'";
        //$arg["btn_close"] = knjCreateBtn($objForm, "btn_close", "閉じる", $extra);
        //v(^|^)_↑

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "ticket", "$pass");
        knjCreateHidden($objForm, "username", "$userid");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::t_include("knjxmenuChg_school.html", $arg);
    }
}
