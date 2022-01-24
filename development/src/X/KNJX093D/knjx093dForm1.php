<?php

require_once('for_php7.php');

class knjx093dForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //出力取込種別ラジオボタン (1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //年度・学期コンボ
        $query = knjx093dquery::getYearSemester();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR_SEMESTER", $model->field["YEAR_SEMESTER"], $extra, 1);

        //学年・コースコンボ
        $query = knjx093dquery::getGradeCourse($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE_COURSE", $model->field["GRADE_COURSE"], $extra, 1);

        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx093dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx093dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    if ($name == "GRADE_COURSE") {
        //MAX文字数取得
        $max_lenG = $max_lenC = 0;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学年
            $zenkakuG = (strlen($row["GRADE_NAME1"]) - mb_strlen($row["GRADE_NAME1"])) / 2;
            $hankakuG = ($zenkakuG > 0) ? mb_strlen($row["GRADE_NAME1"]) - $zenkakuG : mb_strlen($row["GRADE_NAME1"]);
            $max_lenG = ($zenkakuG * 2 + $hankakuG > $max_lenG) ? $zenkakuG * 2 + $hankakuG : $max_lenG;
            //課程学科
            $zenkakuC = (strlen($row["COURSE_MAJORNAME"]) - mb_strlen($row["COURSE_MAJORNAME"])) / 2;
            $hankakuC = ($zenkakuC > 0) ? mb_strlen($row["COURSE_MAJORNAME"]) - $zenkakuC : mb_strlen($row["COURSE_MAJORNAME"]);
            $max_lenC = ($zenkakuC * 2 + $hankakuC > $max_lenC) ? $zenkakuC * 2 + $hankakuC : $max_lenC;
        }
        $result->free();
    }

    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "GRADE_COURSE") {
            //表示調整（学年）
            $zenkakuG = (strlen($row["GRADE_NAME1"]) - mb_strlen($row["GRADE_NAME1"])) / 2;
            $hankakuG = ($zenkakuG > 0) ? mb_strlen($row["GRADE_NAME1"]) - $zenkakuG : mb_strlen($row["GRADE_NAME1"]);
            $grade = $row["GRADE_NAME1"];
            for ($i=0; $i < ($max_lenG - ($zenkakuG * 2 + $hankakuG)); $i++) {
                $grade .= "&nbsp;";
            }
            //表示調整（課程学科）
            $zenkakuC = (strlen($row["COURSE_MAJORNAME"]) - mb_strlen($row["COURSE_MAJORNAME"])) / 2;
            $hankakuC = ($zenkakuC > 0) ? mb_strlen($row["COURSE_MAJORNAME"]) - $zenkakuC : mb_strlen($row["COURSE_MAJORNAME"]);
            $course = $row["COURSE_MAJORNAME"];
            for ($j=0; $j < ($max_lenC - ($zenkakuC * 2 + $hankakuC)); $j++) {
                $course .= "&nbsp;";
            }

            $opt[] = array('label' => $grade.'　('.$row["COURSE_MAJORCD"].')　'.$course.'　('.$row["COURSECODE"].')　'.$row["COURSECODENAME"],
                           'value' => $row["VALUE"]);

            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
    }
    $result->free();

    if ($name == "YEAR_SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR.'-'.CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
