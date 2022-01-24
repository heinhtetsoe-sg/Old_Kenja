<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja110a_remarkForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform3", "POST", "knja110a_remarkindex.php", "", "subform3");

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();
        
        //項目1コンボ
        $query = knja110a_remarkQuery::getBaseRemark($model);
        $extra = "onChange=\"return btn_submit('subform3')\"";
        makeCmb($objForm, $arg, $db, $query, "CODE", $model->field["CODE"], $extra, 1);

        //項目2コンボ
        $query = knja110a_remarkQuery::getBaseRemarkDetail($model);
        $extra = "onChange=\"return btn_submit('subform3')\"";
        makeCmb($objForm, $arg, $db, $query, "SEQ", $model->field["SEQ"], $extra, 1);

        //入力項目を設定
        $query = knja110a_remarkQuery::getRemark($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //QUESTION_CONTENTS
            $extra = "";

            $arg["data"]["QUESTION_CONTENTS"] = $row["QUESTION_CONTENTS"];

            //REMARK
            $setSpase = "";
            $setVal = array();
            $cnt = $row["ANSWER_SELECT_COUNT"];
            if ($row["ANSWER_PATTERN"] == "1") {
                //ラジオボタン
                $opt   = array();
                $extra = array();
                for ($idx=1;$idx<=$cnt;$idx++) {
                    array_push($opt , $idx);
                    array_push($extra , "id=\"REMARK".$idx."\"");
                    $model->field["REMARK"] = ($row["REMARK"] == "") ? "1" : $row["REMARK"];
                }
                $radioArray = knjCreateRadio($objForm, "REMARK", $model->field["REMARK"], $extra, $opt, get_count($opt));
                $setSpase = "";
                $idx = 1;
                foreach($radioArray as $key => $val) { 
                    $setVal["REMARK"] .= $setSpase.$val."<LABEL for=\"{$key}\">".$idx."</LABEL>";
                    $setSpase = "　";
                    $idx++;
                }

            } elseif ($row["ANSWER_PATTERN"] == "2") {
                //チェックボックス
                $array = explode(",", $row["REMARK"]);
                for ($idx=1;$idx<=$cnt;$idx++) {
                    $val = "";
                    $extra  = " id=\"REMARK".$idx."\" ";
                    if (in_array($idx, $array)){
                        $extra .= " checked ";
                    }
                    $val = knjCreateCheckBox($objForm, "REMARK".$idx , "1", $extra);
                    $setVal["REMARK"] .= $setSpase.$val."<LABEL for=\"{$key}\">".$idx."</LABEL>";
                    $setSpase = "　";
                }
            } else {
                //テキスト
                $extra = "";
                $setVal["REMARK"] .= knjCreateTextArea($objForm, "REMARK", 3, 90, "", $extra, $row["REMARK"]);
            }
            $arg["list"][] = $setVal;
            knjCreateHidden($objForm, "QUESTION_CONTENTS", $row["QUESTION_CONTENTS"]);      //設問
            knjCreateHidden($objForm, "ANSWER_PATTERN", $row["ANSWER_PATTERN"]);            //パターン
            knjCreateHidden($objForm, "ANSWER_SELECT_COUNT", $row["ANSWER_SELECT_COUNT"]);  //回答数
        }
        $result->free();
        Query::dbCheckIn($db);

        //戻るボタンを作成
        $link = REQUESTROOT."/A/KNJA110A/knja110aindex.php?cmd=back&SEND_PRGID=KNJD110A_REMARK&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={CTRL_YEAR}&EXP_SEMESTER={CTRL_SEMESTER}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //更新ボタンを作成
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更新", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knja110a_remarkForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array ("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
