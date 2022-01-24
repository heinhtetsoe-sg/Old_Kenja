<?php

require_once('for_php7.php');

class knjz210bform1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz210bindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        $query = knjz210bQuery::getZ010();
        $model->Z010 = $db->getOne($query);

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //コース名
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $query = knjz210bQuery::getCouseName();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["COURSENAME_SET"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["COURSENAME_SET"] = ($model->field["COURSENAME_SET"] && $value_flg) ? $model->field["COURSENAME_SET"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('main');\"";
        $arg["COURSENAME_SET"] = knjCreateCombo($objForm, "COURSENAME_SET", $model->field["COURSENAME_SET"], $opt, $extra, 1);

        //コースコンボの中身を各値にセット
        if ($model->field["COURSENAME_SET"] !="" ) {
            $model->course_set  = explode('-' , $model->field["COURSENAME_SET"]);
            $model->grade      = $model->course_set[0];
            $model->coursecd   = $model->course_set[1];
            $model->majorcd    = $model->course_set[2];
            $model->coursecode = $model->course_set[3];
        }
        
        //教科コンボ作成
        $query = knjz210bQuery::getClassMst($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");

        //科目コンボ作成
        $query = knjz210bQuery::getSubclassMst($model->field["CLASSCD"], $model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //SQL文発行
        $ar[] = array();
        
        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knjz210bQuery::selectQuery($model);
            $result = $db->query($query);
        } else {
            $row =& $model->field;
            $result = "";
        }
        //設定段階値=5 固定
        $cnt = "5";
        for ($i = 1; $i <= $cnt; $i++) {
            if ($result != "") {
                if ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //レコードを連想配列のまま配列$arg[data]に追加していく。 
                    array_walk($row, "htmlspecialchars_array");
                    //小数点を取り除く
                    $ar =explode(".",$row["ASSESSLOW"]);
                    $row["ASSESSLOW"] = $ar[0];
                    $ar =explode(".",$row["ASSESSHIGH"]);
                    $row["ASSESSHIGH"] = $ar[0];
                    $up = $row["UPDATED"];
                }
            }
            $row["ASSESSLEVEL"] = $i;

            //textの有無設定(下限部分)
            $row["ASSESSLOWTEXT"]  = "<input type=\"text\" name=\"";
            $row["ASSESSLOWTEXT"] .= "ASSESSLOW".$row["ASSESSLEVEL"];
            $row["ASSESSLOWTEXT"] .= "\" value=\"";
            if ($result != "") {
                $row["ASSESSLOWTEXT"] .= $row["ASSESSLOW"];
            } else if ($result == "") {
                $row["ASSESSLOWTEXT"] .= $row["ASSESSLOW$i"];
            }
            $row["ASSESSLOWTEXT"] .= "\" size=\"";
            $row["ASSESSLOWTEXT"] .= "4";
            $row["ASSESSLOWTEXT"] .= "\" maxlength=\"";
            $row["ASSESSLOWTEXT"] .= "3";
            $row["ASSESSLOWTEXT"] .= "\" onblur=\"isNumb(this,".($row["ASSESSLEVEL"] -1).",'ELSE');\"";
            $row["ASSESSLOWTEXT"] .= " STYLE=\"text-align: right\"> ";
            $stock[] = $row["ASSESSLOW"];

            //上限部分作成
            //上限値の時
            if ($row["ASSESSLEVEL"] == $cnt){
                $row["ASSESSHIGHTEXT"] = $model->setMax;
            } else {
                $row["ASSESSHIGHTEXT"]  = "<span id=\"strID";
                $row["ASSESSHIGHTEXT"] .= $row["ASSESSLEVEL"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                if ($result != "") {
                    $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                } else if ($result == ""){
                    if ($row["ASSESSLOW".($i + 1)] - 1 > 0) {
                        $row["ASSESSHIGHTEXT"] .= ($row["ASSESSLOW".($i + 1)] - 1);
                    } else {
                        $row["ASSESSHIGHTEXT"] .= "";
                    }
                }
                $row["ASSESSHIGHTEXT"] .= "</span>";
            }
            
            //記号部分作成
            $row["ASSESSMARKTEXT"]  = "<input type=\"text\" name=\"";
            $row["ASSESSMARKTEXT"] .= "ASSESSMARK".$row["ASSESSLEVEL"];
            $row["ASSESSMARKTEXT"] .= "\" value=\"";
            if ($result != "") {
                $row["ASSESSMARKTEXT"] .= $row["ASSESSMARK"];
            } else if ($result == "") {
                $row["ASSESSMARKTEXT"] .= $row["ASSESSMARK$i"];
            }              
            $row["ASSESSMARKTEXT"] .= "\" size=\"";
            $row["ASSESSMARKTEXT"] .= "8";
            $row["ASSESSMARKTEXT"] .= "\" maxlength=\"";
            $row["ASSESSMARKTEXT"] .= "6";
            $row["ASSESSMARKTEXT"] .= "\" STYLE=\"text-align: right\"> ";
                    
            $arg["data"][] = $row;
        }
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $up);
        
        //DB切断
        Query::dbCheckIn($db);
        
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz210bForm1.html", $arg);
    }       
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    //コピーボタンを作成する
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度コピー", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
