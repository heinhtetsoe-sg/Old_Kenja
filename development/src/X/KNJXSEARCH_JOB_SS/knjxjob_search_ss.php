<?php

require_once('for_php7.php');

class knjxjob_search_ss
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("knjxjob_search_ss", "POST", "knjxjob_search_ssindex.php", "", "knjxjob_search_ss");
        $db = Query::dbCheckOut();

        $arg["jscript"] = "setF('".$model->frame."');";

        //ラジオボタン
        $opt = array(1, 2); //1：部分一致 2：先頭一致
        $model->field["SORT_TYPE"] = ($model->field["SORT_TYPE"] == "") ? "1" : $model->field["SORT_TYPE"];
        $extra = array("id=\"SORT_TYPE1\"", "id=\"SORT_TYPE2\"");
        $radioArray = knjCreateRadio($objForm, "SORT_TYPE", $model->field["SORT_TYPE"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //学校名
        $extra = "onKeyPress=\"return submitStop(event)\";\"";
        $arg["JOBTYPE_SNAME"] = knjCreateTextBox($objForm, $model->field["JOBTYPE_SNAME"], "JOBTYPE_SNAME", 32, 32, $extra);

        //学校名リスト
        $extra = "ondblclick=\"apply_school(this)\" style=\"width:300px;\"";
        $query = knjxjob_search_ssQuery::getSchoolList($model);
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        $value = "";
        $value_flg = false;
        $cnt = 1;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $spaces = make_space('あああああああ', $row["JOBTYPE_SNAME"]);
            $label = "{$row["JOBTYPE_SNAME"]} {$spaces} {$row["JOBTYPE_LNAME"]}";
            $opt[] = array('label' => $label,
                            "value" => $row["VALUE"]);

            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $result->free();

        $arg["JOB_LIST"] = knjCreateCombo($objForm, "JOB_LIST", '999999999', $opt, $extra, 10);

        /**********/
        /* ボタン */
        /**********/
        //検索
        $extra = "onclick=\"return btn_submit('search')\"";
        $arg["button"]["search"] = knjCreateBtn($objForm, "search", "検 索", $extra);
        //戻る
        $extra = "onClick=\"parent.closeit();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "target_number", $model->target_number);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxjob_search_ss.html", $arg);
    }
}

/****************************/
/* ネスト用のスペースの生成 */
/****************************/
function make_space($longer_name, $name)
{
    $mojisuu_no_sa = strlen($longer_name) - strlen($name);
    $spaces = '　';
    for ($i = 0; $i < $mojisuu_no_sa / 3; $i++) {
        $spaces .= '　';
    }

    return $spaces;
}
