<?php
class knjl417hForm1 {

    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl417hindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->examYear;

        //入試区分
        if ($model->field["APPLICANTDIV"] == "1") {
            $arg["APPLICANTDIV_J"] = "1";
        }

        //学校種別コンボボックス
        $extra = "onchange=\"return btn_submit('changeApp');\" tabindex=-1";
        $query = knjl417hQuery::getNameMst($model->examYear, "L003");
        $arg["TOP"]["APPLICANTDIV"] = makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //受験コースコンボ
        $model->field["EXAMCOURSECD"] = ($model->cmd == "changeApp") ? "": $model->field["EXAMCOURSECD"];
        $query = knjl417hQuery::getExamCourseMst($model);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $arg["TOP"]["EXAMCOURSECD"] = makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->field["EXAMCOURSECD"], $extra, 1, "");

        //マスタ取得
        $getMstArray = function ($db, $query) {
            $mstArray = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $mstArray[$row["KEY"]] = $row["NAME"];
            }
            return $mstArray;
        };

        //コース名取得
        $query = knjl417hQuery::getExamCourseMst($model);
        $examCourseMst = $getMstArray($db, $query);

        //一覧表示
        $model->arr_examno = array();
        if ($model->s_examno != "" && $model->field["APPLICANTDIV"] != "" && $model->field["EXAMCOURSECD"] != "") {
            //データ取得
            $result = $db->query(knjl417hQuery::selectQuery($model));
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //HIDDENに保持する用
                $examno = $row["EXAMNO"];
                $model->arr_examno[] = $examno;

                //内部関数定義
                if ($count == 0) {
                    $makeScholarCmb = function ($objForm, $name, $value, $extra) {
                        $opt = array();
                        $opt[] = array("value" => "", "label" => "");
                        $opt[] = array("value" => 1, "label" => "半額");
                        $opt[] = array("value" => 2, "label" => "全額");
                        return knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
                    };
                }

                //内申特別奨学生判定
                $extra = " style=\"font-size:11px;\" disabled ";
                $row["NAISHIN_SCHOLAR"] = $makeScholarCmb($objForm, "NAISHIN_SCHOLAR", $row["NAISHIN_SCHOLAR"], $extra);

                //模試名
                $extra = " style=\"width:110px;font-size:11px;\" disabled ";
                $query = knjl417hQuery::getEntexamSettingMst($model, "L100");
                $row["MOSHI_NAME"] = makeCmb($objForm, $arg, $db, $query, "MOSHI_NAME", $row["MOSHI_NAME"], $extra, 1);
                //模試成績
                $extra = "style=\"text-align:right;background-color:lightgray\" readonly";
                $row["MOSHI_SCORE"] = knjCreateTextBox($objForm, $row["MOSHI_SCORE"], "MOSHI_SCORE", 2, 2, $extra);
                //模試特別奨学生判定
                $extra = " style=\"font-size:11px;\" disabled ";
                $row["MOSHI_SCHOLAR"] = $makeScholarCmb($objForm, "MOSHI_SCHOLAR", $row["MOSHI_SCHOLAR"], $extra);

                //OPT判定
                $extra = " style=\"width:80px;font-size:11px;\" disabled ";
                $query = knjl417hQuery::getEntexamSettingMst($model, "L101");
                $row["OPT_JUDGE"] = makeCmb($objForm, $arg, $db, $query, "OPT_JUDGE", $row["OPT_JUDGE"], $extra, 1);
                //OPT特別奨学生判定
                $extra = " style=\"font-size:11px;\" disabled ";
                $row["OPT_SCHOLAR"] = $makeScholarCmb($objForm, "OPT_SCHOLAR", $row["OPT_SCHOLAR"], $extra);

                //成績優秀特別奨学生判定
                $extra = " style=\"font-size:11px;\" disabled ";
                $row["SCORE_SCHOLAR1"] = $makeScholarCmb($objForm, "SCORE_SCHOLAR1", $row["SCORE_SCHOLAR1"], $extra);
                $extra = " style=\"font-size:11px;\" disabled ";
                $row["SCORE_SCHOLAR2"] = $makeScholarCmb($objForm, "SCORE_SCHOLAR2", $row["SCORE_SCHOLAR2"], $extra);
                $extra = " style=\"font-size:11px;\" disabled ";
                $row["SCORE_SCHOLAR3"] = $makeScholarCmb($objForm, "SCORE_SCHOLAR3", $row["SCORE_SCHOLAR3"], $extra);
                $extra = " style=\"font-size:11px;\" disabled ";
                $row["SCORE_SCHOLAR4"] = $makeScholarCmb($objForm, "SCORE_SCHOLAR4", $row["SCORE_SCHOLAR4"], $extra);
                $extra = " style=\"font-size:11px;\" disabled ";
                $row["SCORE_SCHOLAR5"] = $makeScholarCmb($objForm, "SCORE_SCHOLAR5", $row["SCORE_SCHOLAR5"], $extra);
                

                //最終内諾特別奨学生判定
                $extra = " style=\"font-size:11px;\" ";
                $row["LAST_SCHOLAR"] = $makeScholarCmb($objForm, "LAST_SCHOLAR-".$examno, $row["LAST_SCHOLAR"], $extra);

                //最終内諾判定区分
                $extra = " style=\"width:70px;font-size:11px;\" ";
                $query = knjl417hQuery::getEntexamSettingMst($model, "L102");
                $value = (isset($model->warning)) ? $model->examData[$examno]["LAST_SCHOLAR_DIV"] : $row["LAST_SCHOLAR_DIV"];
                $row["LAST_SCHOLAR_DIV"] =  makeCmb($objForm, $arg, $db, $query, "LAST_SCHOLAR_DIV-".$examno, $row["LAST_SCHOLAR_DIV"], $extra, 1, "BLANK");

                $dataflg = true;

                $arg["data"][] = $row;
                $count++;
            }

            if ($count == 0) {
                $model->setMessage("MSG303");
            }
        }

        //開始受験番号テキストボックス
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->s_examno, "S_EXAMNO", 5, 5, $extra);
        //次へボタン
        $extra = "onclick=\"return btn_submit('next');\"";
        $arg["BUTTON"]["NEXT"] = knjCreateBtn($objForm, "btn_next", ">>", $extra);
        
        //今へボタン
        $extra = "onclick=\"return btn_submit('now');\"";
        $arg["BUTTON"]["NOW"] = knjCreateBtn($objForm, "btn_now", " = ", $extra);
        
        //前へボタン
        $extra = "onclick=\"return btn_submit('back');\"";
        $arg["BUTTON"]["BACK"] = knjCreateBtn($objForm, "btn_back", "<<", $extra);


        //ボタン作成
        makeBtn($objForm, $arg, $dataflg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COUNT", $count);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL417H");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl417hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "    ", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $dataflg) {

    $disable  = ($dataflg) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\" tabindex=-1".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\" tabindex=-1".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\" tabindex=-1";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
