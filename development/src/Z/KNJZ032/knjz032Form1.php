<?php

require_once('for_php7.php');

class knjz032Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz032index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        
        //生徒表示
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;
        
        //対象年度
        $arg["YEAR_SHOW"] = $model->year.'年度';

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //学期数の取得(学年末分も含める）
        $model->semescount = ($db->getOne(knjz032Query::getSemesterCount($model)));
        
        //学年コンボ
        $query = knjz032Query::getGrade($model);
        $extra = "onchange=\"btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1);
        
        //対象データを取得
        $counter = 1;
        $query = knjz032Query::getSelectData($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            if (isset($model->warning)) {
                $row["SDATE"] = $model->SDATE["$counter"];
                $row["EDATE"] = $model->EDATE["$counter"];
                $row["SEMESTERNAME"] = $model->SEMESTERNAME["$counter"];
            }
            //反映時
            if ($model->cmd == "hanei") {
                $row["SDATE"] = $row["SEM_SDATE"];
                $row["EDATE"] = $row["SEM_EDATE"];
                $row["SEMESTERNAME"] = $row["SEM_SEMESTERNAME"];
            }
            
            //カレンダー
            if ($row["SEMESTER"] == "9") {
                $row["SDATE"] = str_replace("-","/",$row["SDATE"]);
                $row["EDATE"] = str_replace("-","/",$row["EDATE"]);
                
                knjCreateHidden($objForm, "SDATE".$counter, str_replace("-", "/", $row["SDATE"]));
                knjCreateHidden($objForm, "EDATE".$counter, str_replace("-", "/", $row["EDATE"]));
            } else {
                $row["SDATE"]=View::popUpCalendar($objForm,"SDATE".$counter,str_replace("-", "/", $row["SDATE"]));
                $row["EDATE"]=View::popUpCalendar($objForm,"EDATE".$counter,str_replace("-", "/", $row["EDATE"]));
            }

            //学期名称
            $extra = "";
            $row["SEMESTERNAME"] = knjCreateTextBox($objForm, $row["SEMESTERNAME"], "SEMESTERNAME".$counter, 10, 15, $extra);

            //SEMESTER(hidden)
            knjCreateHidden($objForm, "SEMESTER".$counter, $row["SEMESTER"]);
            $counter++;
            $arg["data"][] = $row;
        }


        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz032Form1.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $query = knjz032Query::getRecordList($model, "");
    $result = $db->query($query);
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //レコードを連想配列のまま配列$arg[data]に追加していく。
        array_walk($rowlist, "htmlspecialchars_array");
        
        if ($bifKey !== $rowlist["GRADE"]) {
            $cnt = $db->getOne(knjz032Query::getRecordList($model, $rowlist["GRADE"]));
            $rowlist["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
        }
        $bifKey = $rowlist["GRADE"];
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //学期マスタ反映ボタンを作成する
    $extra = "onclick=\"return btn_submit('hanei');\"";
    $arg["button"]["btn_hanei"] = knjCreateBtn($objForm, "btn_hanei", "学期マスタ反映", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消しボタンを作成する
    $extra = "onclick=\"return btn_submit('clear')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタンを作成する
    $link = REQUESTROOT."/Z/KNJZ030/knjz030index.php";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJZ032");
}

?>
