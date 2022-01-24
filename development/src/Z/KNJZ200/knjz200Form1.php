<?php

require_once('for_php7.php');

class knjz200Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz200index.php", "", "edit");
        $arg["YEAR"] = CTRL_YEAR;
        $arg["PROGRAMID"] =PROGRAMID;

        $db = Query::dbCheckOut();

        //校種コンボ
        $opt = array();
        $value_flg = false;
        $opt[] = array('label' => "-- 全て --", 'value' => "ALL");
        $query = knjz200Query::getSchoolKind($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($model->sch_kind == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $model->sch_kind = ($model->sch_kind != "" && $value_flg) ? $model->sch_kind : $opt[0]["value"];

        $extra = "onchange=\"return btn_submit('coursename');\"";
        $arg["SCH_KIND"] = knjCreateCombo($objForm, "SCH_KIND", $model->sch_kind, $opt, $extra, 1);

        //学籍在籍データ件数
        $regd_cnt = $db->getOne(knjz200Query::getRegdDatCnt());
        $flg = ($regd_cnt > 0) ? "" : 1;

        //実授業の場合、欠課数上限値は表示しない
        $query = knjz200Query::getJugyouJisuFlg();
        $jugyou_jisu_flg = $db->getOne($query); //1:法定授業 2:実授業
        $arg["ROWSPAN"] = "1";
        if ($jugyou_jisu_flg != '2') {
            $arg["show"]["ABSENCE_HIGH_SHOW"] = "show";
            $arg["ROWSPAN"] = "2";
        }

        //各名称の文字数のMAX値取得
        $item = array("COURSENAME", "MAJORNAME", "COURSECODENAME", "GRADE_NAME1");
        $maxLen = array();
        $maxLen_name = array();
        $result = $db->query(knjz200Query::getCouseName($model, $flg, "ALL"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($item as $key) {
                $zenkaku = $hankaku = 0;
                $zenkaku = (strlen($row[$key]) - mb_strlen($row[$key])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row[$key]) - $zenkaku : mb_strlen($row[$key]);
                $maxLen[$key] = ($zenkaku * 2 + $hankaku > $maxLen[$key]) ? $zenkaku * 2 + $hankaku : $maxLen[$key];
            }
        }

        //コースコンボ作成
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjz200Query::getCouseName($model, $flg, $model->sch_kind));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //名称の表示調整
            foreach ($item as $key) {
                $row[$key] = adjustString($row[$key], $maxLen[$key]);
            }

            $value = $row["COURSECODE"]." ".$row["COURSECD"]." ".$row["MAJORCD"]." ".$row["GRADE"];

            $opt[] = array("label" => $row["GRADE_NAME1"]."&nbsp;&nbsp;".
                                      "(".$row["COURSECD"].$row["MAJORCD"].")&nbsp;".
                                      $row["COURSENAME"].$row["MAJORNAME"]."&nbsp;&nbsp;".
                                      "(".$row["COURSECODE"].")&nbsp;".
                                      $row["COURSECODENAME"],
                           "value" => $value);

            if ($model->coursename == $value) {
                $value_flg = true;
            }
        }
        $model->coursename = ($model->coursename != "" && $value_flg) ? $model->coursename : $opt[0]["value"];
        //コース名
        $objForm->ae(array("type"      => "select",
                           "name"       => "COURSENAME",
                           "size"       => "1",
                           "value"      => $model->coursename,
                           "extrahtml"  => " onchange=\"btn_submit('coursename');\"",
                           "options"    => $opt
                           ));
        $arg["COURSENAME"] = $objForm->ge("COURSENAME");

        //欠課数オーバーのタイトル
        if (in_array("1", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN"]  = $model->control["学期名"]["1"];
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN2"] = $model->control["学期名"]["2"];
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN3"] = $model->control["学期名"]["3"];
        }

        //コース一覧取得
        //教育課程用
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        //掛け率
        if ($model->Properties["useMultiplicationRate"] == '1') {
            $arg["useRate"] = "1";
        }
        //時間単位取得
        if ($model->Properties["useTimeUnit"] == '1') {
            $arg["useTimeUnit"] = "1";
        }
        $result = $db->query(knjz200Query::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["REQUIRE_FLG"] = ($row["REQUIRE_FLG"]=="")?"":$row["NAME1"];
            $row["AUTHORIZE_FLG"] = ($row["AUTHORIZE_FLG"]=="1")?"半期":"";
            $row["COMP_UNCONDITION_FLG"] = ($row["COMP_UNCONDITION_FLG"]=="1")?"無条件":"";
            if ($row["SUBCLASSCD"] == $model->subclasscd) {
                $row["CREDITS"] = ($row["CREDITS"]) ? $row["CREDITS"] : "　";
                $row["CREDITS"] = "<a name=\"target\">{$row["CREDITS"]}</a><script>location.href='#target';</script>";
            }
            $row["SCH_KIND"]    = $model->sch_kind;
            $row["COURSENAME"]  = $model->coursename;
            $arg["data"][] = $row;
        }
        $result->free();

        Query::dbCheckIn($db);

        //コピーボタン
        $extra = " onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        if ($model->cmd == "list" || VARS::post("cmd") == "coursename") {
            $arg["reload"] = "window.open('knjz200index.php?cmd=edit&SCH_KIND={$model->sch_kind}&COURSENAME={$model->coursename}','right_frame');";
        }

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz200Form1.html", $arg);
    }
}

//表示の調整
function adjustString($string, $max_len)
{
    $zenkaku = (strlen($string) - mb_strlen($string)) / 2;
    $hankaku = ($zenkaku > 0) ? mb_strlen($string) - $zenkaku : mb_strlen($string);
    $len = $zenkaku * 2 + $hankaku;
    $label = $string;
    for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) {
        $label .= "&nbsp;";
    }

    return $label;
}
