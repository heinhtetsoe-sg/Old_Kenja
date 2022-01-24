<?php

require_once('for_php7.php');

class knja125kForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja125kindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //ねらい一覧
        $datacnt = 0;
        $cd = "";
        $query = knja125kQuery::getViewPoint($model, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($cd == $row["POINT_L_CD"]) {
                $row["ROWSPAN"] = "";
            } else {
                //縦書き用に1文字ごとに改行を入れる
                $strlen = mb_strlen($row["REMARK_L"], 'UTF-8');
                $remarkL = array();
                for ($i = 0; $i < $strlen; $i += 1) {
                    $remarkL[] =  mb_substr($row["REMARK_L"], $i, 1);
                }
                $row["REMARK_L"] = implode("<br>", $remarkL);

                $row["ROWSPAN"] = $db->getOne(knja125kQuery::getViewPoint($model, "cnt", $row["POINT_L_CD"]));
            }

            $arg["view"][] = $row;

            $cd = $row["POINT_L_CD"];
            $datacnt++;
        }
        $result->free();

        //空行挿入
        if ($datacnt < 15) {
            for ($i=0; $i < 15-$datacnt; $i++) {
                $row["ROWSPAN"] = "1";
                $row["REMARK_L"] = "";
                $row["REMARK_M"] = "";
                $arg["view"][] = $row;
            }
        }

        //所見データ取得
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $Row = $db->getRow(knja125kQuery::getHtrainremarkKDat($model), DB_FETCHMODE_ASSOC);
            if ($model->Properties["useSpecial_Support_School"] == "1") {
                $det2Dat = $db->getRow(knja125kQuery::getHtrainremarkDetail2Dat($model), DB_FETCHMODE_ASSOC);
                $Row["INDEPENDENCE_REMARK"] = $det2Dat["REMARK1"];
                $det2HDat = $db->getRow(knja125kQuery::getHtrainremarkDetail2HDat($model), DB_FETCHMODE_ASSOC);
                $Row["FIELD1"] = $det2HDat["REMARK1"];
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row =& $model->field;
        }

        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $arg["data"]["JUTEN_ROWSPAN"] = "3";
            $arg["data"]["INDEPENDENCE_REMARK"] = $model->textSize["INDEPENDENCE_REMARK"];
            $arg["data"]["INDEPENDENCE_ROWSPAN"] = "1";
        } else {
            $arg["data"]["JUTEN_ROWSPAN"] = "2";
            $arg["data"]["INDEPENDENCE_ROWSPAN"] = "2";
        }
        //所見テキストボックス作成
        foreach ($model->textSize as $key => $val) {
            $height = (int)$val["gyo"] * 13.5 + ((int)$val["gyo"] - 1) * 3 + 5;
            $arg["data"][$key] = KnjCreateTextArea($objForm, $key, $val["gyo"], ((int)$val["moji"] * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row[$key]);
            $arg["data"][$key."_COMMENT"] = "(全角{$val["moji"]}文字X{$val["gyo"]}行まで)";
        }

        //学校種別
        $schoolkind = $db->getOne(knja125kQuery::getSchoolKind($model));

        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $arg["data"]["ATTENDREC_REMARK_TITLE"] = "備考";
        } else {
            $arg["data"]["ATTENDREC_REMARK_TITLE"] = implode("<br>", array("出欠状況の", "備考"));
        }

        //生徒項目名切替処理
        $sch_label = "";
        //テーブルの有無チェック
        $query = knja125kQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && $schoolkind) {
            //生徒項目名取得
            $sch_label = $db->getOne(knja125kQuery::getSchName($model, $schoolkind));
        }
        $sch_label = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        if ((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'K') {
            //更新ボタン
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
            //前の生徒へボタン
            $extra = "style=\"width:130px\" onclick=\"parent.left_frame.nextStudentOnly('pre');\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "前の幼児へ", $extra);
            //次の生徒へボタン
            $extra = "style=\"width:130px\" onclick=\"parent.left_frame.nextStudentOnly('next');\"";
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "次の幼児へ", $extra);
        } else {
            //更新ボタン
            $extra = "onclick=\"return btn_submit('update');\"";
            $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //更新後前の生徒へボタン
            $extra = "style=\"width:130px\" onclick=\"updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
            $arg["button"]["btn_up_pre"] = KnjCreateBtn($objForm, "btn_up_pre", "更新後前の幼児へ", $extra);
            //更新後次の生徒へボタン
            $extra = "style=\"width:130px\" onclick=\"updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
            $arg["button"]["btn_up_next"] = KnjCreateBtn($objForm, "btn_up_next", "更新後次の幼児へ", $extra);
        }

        $titles = "";
        $fieldSize = "";
        $comma = "";
        foreach ($model->textSize as $key => $val) {
            $titles .= $comma . $key."=".$val["title"];
            $fieldSize .= $comma . $key."=".$val["moji"]."|".$val["gyo"];
            $comma = ",";
        }

        if ($model->Properties["useSpecial_Support_School"] != "1") {
            //CSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A125K/knjx_a125kindex.php?FIELDSIZE=".$fieldSize."&TITLES=".$titles."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);
        }

        if ($model->schregno == "") {
            $disabled = " disabled";
        } else {
            $disabled = "";
        }

        //教育支援計画参照ボタン
        $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SIENKEIKAKU/knjx_sienkeikakuindex.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&NAME={$model->name}',0,document.documentElement.scrollTop || document.body.scrollTop,350,450);\"";
        $arg["button"]["btn_sienkeikaku"] = KnjCreateBtn($objForm, "btn_sienkeikaku", "教育支援計画参照", $extra.$disabled);

        //既入力内容の参照ボタン
        $extra = " onclick=\"loadwindow('knja125kindex.php?cmd=subform1&YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&NAME={$model->name}&KINYUURYOKUTYPE=ACT',0,document.documentElement.scrollTop || document.body.scrollTop,800,400);\"";
        $arg["button"]["btn_kinyuuryoku1"] = KnjCreateBtn($objForm, "btn_kinyuuryoku1", "既入力内容の参照", $extra.$disabled);

        //既入力内容の参照ボタン
        $extra = " onclick=\"loadwindow('knja125kindex.php?cmd=subform1&YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&NAME={$model->name}&KINYUURYOKUTYPE=VAL',0,document.documentElement.scrollTop || document.body.scrollTop,800,400);\"";
        $arg["button"]["btn_kinyuuryoku2"] = KnjCreateBtn($objForm, "btn_kinyuuryoku2", "既入力内容の参照", $extra.$disabled);

        //既入力内容の参照ボタン
        $extra = " onclick=\"loadwindow('knja125kindex.php?cmd=subform2&YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&NAME={$model->name}&KINYUURYOKUTYPE=VAL',0,document.documentElement.scrollTop || document.body.scrollTop,500,500);\"";
        $arg["button"]["btn_kinyuuryoku3"] = KnjCreateBtn($objForm, "btn_kinyuuryoku3", "既入力内容の参照", $extra.$disabled);

        //既入力内容の参照ボタン
        $extra = " onclick=\"loadwindow('knja125kindex.php?cmd=subform3&YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&NAME={$model->name}&KINYUURYOKUTYPE=VAL',0,document.documentElement.scrollTop || document.body.scrollTop,800,600);\"";
        $arg["button"]["btn_kinyuuryoku4"] = KnjCreateBtn($objForm, "btn_kinyuuryoku4", "既入力内容の参照", $extra.$disabled);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd == "clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja125kForm1.html", $arg);
    }
}
