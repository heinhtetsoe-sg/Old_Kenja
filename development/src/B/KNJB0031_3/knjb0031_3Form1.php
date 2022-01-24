<?php

require_once('for_php7.php');

class knjb0031_3Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb0031_3index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["TERM_SHOW"] = $db->getOne(knjb0031_3Query::getSemesterMst($model));

        //コース
        $gcRow = $db->getRow(knjb0031_3Query::getGradeCouse($model), DB_FETCHMODE_ASSOC);
        $gcLabel  = $gcRow["GRADE_NAME1"]."&nbsp;";
        $gcLabel .= "(".$gcRow["COURSECD"].$gcRow["MAJORCD"].")&nbsp;".$gcRow["COURSENAME"].$gcRow["MAJORNAME"]."&nbsp;";
        $gcLabel .= "(".$gcRow["COURSECODE"].")&nbsp;".$gcRow["COURSECODENAME"];
        $arg["GRADE_COURSE_SHOW"] = $gcLabel;

        //科目
        $arg["SUBCLASS_SHOW"] = $db->getOne(knjb0031_3Query::getSubclassMst($model));

        //群ラジオ 1:教科 2:群
        $arg["GROUP_SHOW"] = ($model->group == "2") ? "群" : "教科";

        //講座一覧
        $chairList = array();

        if (!isset($model->warning)) {
            //ＤＢ情報
            $query = knjb0031_3Query::getChairList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $chairList[] = $row;
            }
            $result->free();
        } else {
            //更新時のチェックでエラーの場合、画面情報をセット
            for ($counter = 0; $counter < $model->data_cnt; $counter++) {
                $Row = array();
                foreach ($model->fields as $key => $val) {
                    $Row[$key] = $val[$counter];
                }
                $chairList[] = $Row;
            }
        }

        //講座一覧の表示件数
        knjCreateHidden($objForm, "DATA_CNT", get_count($chairList));

        //講座一覧を表示
        foreach ($chairList as $counter => $Row) {

            //講座コード
            $extra = "onblur=\"this.value=toInteger(this.value);\"";
            if (strlen($Row["UPDATED"])) {
                $extra .= " STYLE=\"background-color:darkgray\" "."readonly";
            }
            $setData["CHAIRCD"] = knjCreateTextBox($objForm, $Row["CHAIRCD"], "CHAIRCD"."-".$counter, 8, 7, $extra);

            //講座名称
            if (strlen($Row["UPDATED"])) {
                $extra = "STYLE=\"WIDTH:100%; background-color:darkgray\" "."readonly";
            } else {
                $extra = "STYLE=\"WIDTH:100%;\"";
            }
            $setData["CHAIRNAME"] = knjCreateTextBox($objForm, $Row["CHAIRNAME"], "CHAIRNAME"."-".$counter, 31, 30, $extra);

            //講座略称
            if (strlen($Row["UPDATED"])) {
                $extra = "STYLE=\"WIDTH:100%; background-color:darkgray\" "."readonly";
            } else {
                $extra = "STYLE=\"WIDTH:100%;\"";
            }
            $setData["CHAIRABBV"] = knjCreateTextBox($objForm, $Row["CHAIRABBV"], "CHAIRABBV"."-".$counter, 16, 15, $extra);

            //使用施設(初期化)
            $Row_Fac = array();
            $maxNo = 3;

            //レコード取得（使用施設）
            if (!isset($model->warning)) {
                $no = 1;
                $result = $db->query(knjb0031_3Query::getFac($model->term, $Row["CHAIRCD"]));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $Row_Fac["FACCD".$no] = $row["FACCD"];
                    $Row_Fac["FACILITYABBV".$no] = $row["FACILITYABBV"];
                    $no++;
                    if ($maxNo < $no) break;
                }
                $result->free();
            } else {
                for ($no = 1; $no <= $maxNo; $no++) {
                    $Row_Fac["FACCD".$no] = $model->fields["FACCD".$no][$counter];
                    $Row_Fac["FACILITYABBV".$no] = $model->fields["FACILITYABBV".$no][$counter];
                }
            }

            for ($no = 1; $no <= $maxNo; $no++) {
                //使用施設コード
                $name = "FACCD".$no;
                $extra = "onblur=\"this.value=toInteger(this.value);\" onPaste=\"return showPaste(this);\"";
                $setData[$name] = knjCreateTextBox($objForm, $Row_Fac[$name], $name."-".$counter, 5, 4, $extra);
                //使用施設略称
                $name = "FACILITYABBV".$no;
                $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\" onPaste=\"return showPaste(this);\"";
                $setData[$name] = knjCreateTextBox($objForm, $Row_Fac[$name], $name."-".$counter, 31, 30, $extra);
            }

            $arg["data"][] = $setData;

            knjCreateHidden($objForm, "UPDATED"."-".$counter, $Row["UPDATED"]);

        } //foreach

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjb0031_3Form1.html", $arg); 
    }
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻る
    $url = REQUESTROOT."/B/KNJB0031/knjb0031index.php";
    $param = "?cmd=edit&term={$model->term}&GRADE_COURSE={$model->grade_course}&SUBCLASSCD={$model->subclasscd}&GROUP={$model->group}";
    $extra = "onclick=\"window.open('{$url}{$param}','_self');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    //ＣＳＶ出力
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
