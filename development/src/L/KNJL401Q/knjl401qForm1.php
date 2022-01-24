<?php

require_once('for_php7.php');

class knjl401qForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl401qindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        if (in_array($model->cmd, array("search"))) {
            $query = knjl401qQuery::getList($model);
            $result = $db->query($query);
            $found = false;

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->field["EXAMNO"] = $row["SAT_NO"];
                $model->field["NAME_SEI"] = $row["LASTNAME"];
                $model->field["NAME_MEI"] = $row["FIRSTNAME"];
                $model->field["PLACECD"] = $row["PLACECD"];
                $model->field["APPLYDIV"] = $row["IND_KUBUN"];
                $model->field["FEE"] = $row["SEND_KUBUN"];
                $model->field["BAN_EXAMNO"] = "";
                $found = true;
                break;
            }
            $result->free();
            if ($found == false) {
                $model->field["PLACECD"] = "";
                $model->field["FEE"] = "";
                $model->field["APPLYDIV"] = "";
                $model->field["BAN_EXAMNO"] = "";
                $model->field["NAME_SEI"] = "";
                $model->field["NAME_MEI"] = "";
                $model->setWarning("MSG303" ,"　　（受験番号）");
            }
        } else if ($model->cmd == 'ban') {
            $checkQuery = knjl401qQuery::getNoGroup($model);
            $result = $db->query($checkQuery);
            $found = false;

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->field["BAN_EXAMNO"] = $row["NEXT"];
                $found = true;
            }
            if ($found == false) {
                $model->setWarning("MSG303" ,"　　（受験番号帯）");
                $model->field["BAN_EXAMNO"] = "";
            }
        } else if ($model->cmd == 'cancel') {
                $model->field["EXAMNO"] = $row["SAT_NO"];
                $model->field["NAME_SEI"] = "";
                $model->field["NAME_MEI"] = "";
                $model->field["PLACECD"] = "";
                $model->field["FEE"] = "";
                $model->field["APPLYDIV"] = "";
                $model->field["BAN_EXAMNO"] = "";
        }

        //受験番号
        $extra = "";
        $arg["EXAMNO"] = knjCreateTextBox($objForm, $model->field["EXAMNO"], "EXAMNO", 10, 5, $extra);

        
        //氏名(姓)
        $extra = "";
        $arg["NAME_SEI"] = knjCreateTextBox($objForm, $model->field["NAME_SEI"], "NAME_SEI", 20, 10, $extra);
        //氏名(名)
        $extra = "";
        $arg["NAME_MEI"] = knjCreateTextBox($objForm, $model->field["NAME_MEI"], "NAME_MEI", 20, 10, $extra);
        
        //試験会場
        $placeQuery = knjl401qQuery::getPlace();
        $placeResult = $db->query($placeQuery);
        $opt = array();
        $opt[0] = array("value"     =>  "",
                        "label"     =>  "");
        while($placeRow = $placeResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value"  => $placeRow["PLACECD"],
                           "label"  => $placeRow["PLACECD"]."：".$placeRow["PLACEAREA"]);
        }
        $extra = "";
        $arg["PLACECD"] = knjCreateCombo($objForm, "PLACECD", $model->field["PLACECD"], $opt, $extra, 1);

        // 受験料徴収有無 1:未徴収 2:徴収済
        $opt = array(1, 2);
        $extra = array("id=\"FEE1\" "
                     , "id=\"FEE2\" "
                      );
        $radioArray = knjCreateRadio($objForm, "FEE", $model->field["FEE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        // 申込区分 1:個人 2:団体 3:校内生
        $opt = array(1, 2, 3);
        $extra = array("id=\"APPLYDIV1\" "
                     , "id=\"APPLYDIV2\" "
                     , "id=\"APPLYDIV3\" "
                      );
        $radioArray = knjCreateRadio($objForm, "APPLYDIV", $model->field["APPLYDIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //発番 受験番号
        $extra = "";
        $arg["BAN_EXAMNO"] = knjCreateTextBox($objForm, $model->field["BAN_EXAMNO"], "BAN_EXAMNO", 10, 5, $extra);
        

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL401Q");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl401qForm1.html", $arg); 
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    // 検索ボタン
    $extra = "onclick=\"btn_submit('search');\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);
    // 氏名検索ボタン
    $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL401Q/knjl401qindex.php?cmd=name_search', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 240)\"";
    $arg["button"]["btn_name_search"] = View::setIframeJs().knjCreateBtn($objForm, "btn_namesearch", "氏名検索", $extra);
    // 発番ボタン
    $extra = "onclick=\"btn_submit('ban');\"";
    $arg["button"]["btn_ban"] = knjCreateBtn($objForm, "btn_ban", "発 番", $extra);
    // 追加ボタン
    $extra = "onclick=\"btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    // 取消ボタン
    $extra = "onclick=\"btn_submit('cancel');\"";
    $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
    // 受験票プレビュー/印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "受験票プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
