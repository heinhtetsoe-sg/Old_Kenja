<?php

require_once('for_php7.php');

class knjx410Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjx410Form1", "POST", "knjx410index.php", "", "knjx410Form1");

        if ($model->Properties["useHttps"] == "1" && $model->cmd == "KNJX400S") {
            $arg["jscript"] = "collHttps('".REQUESTROOT."', 'main', '{$model->auth}')";
        }

        $width  = array("450","100","*");
        $tables = new Table($header, $width);

        $arg["maintenance_kenja"] = "<a href=\"".REQUESTROOT."/maintenance/maintenance.html\" target=\"_blank\" accesskey=?></a>";

        $db = Query::dbCheckOut();

        global $auth;

        $image = array("FoldClose.gif","Display1.gif", "Display2.gif","Printer.gif", "Update.gif");
        //教科、科目、クラス取得
        $result = $db->query(knjx410Query::selectQuery($model->properties, $model->menuid, $auth->auth["ADMINGRP_FLG"], 2, $model->pastyear));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            if (strtoupper($row["PROGRAMID"]) == "TITLE") {
                $data = array($row["MENUNAME"],"Program_ID","Menu_ID","TITLE");
            } else {
                $url = REQUESTROOT.$row["PROGRAMPATH"]."/".strtolower($row["PROGRAMID"])."index.php?PROGRAMID={$row["PROGRAMID"]}&SEND_PRGID=KNJX410&SEND_AUTH={$model->auth}";
                $title = $model->menuname ."-[" .$row["MENUNAME"] ."]　".STAFFNAME_SHOW;
                $title = urlencode($title);

                //リンク設定
                $js  = " wopen('".$url."'";
                $js .= ",'SUBWIN1',0,0,screen.availWidth,screen.availHeight);";
                $row["NAME"] = View::alink("#", htmlspecialchars($row["MENUNAME"]), "onClick=\"$js\"");
                $img = "<img src=\"".REQUESTROOT ."/image/system/" .$image[$row["PROCESSCD"]]."\" width=\"16\" height=\"16\">";
                $data = array($img ."&nbsp;".$row["NAME"],
                              $row["PROGRAMID"],
                              $row["MENUID"]);
            }
            $tables->addData($data);
        }

        $tables->setFrameHeight(500);

        $arg["table"] = $tables->toTable();
        $arg["REQUESTROOT"] = REQUESTROOT;

        //終了
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"deleteCookie(); closeWin();\"");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PROGRAMID");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx410Form1.html", $arg);
    }
}
?>
