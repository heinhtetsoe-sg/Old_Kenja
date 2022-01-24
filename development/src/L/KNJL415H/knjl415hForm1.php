<?php
class knjl415hForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl415hindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->examYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('changeApp');\" tabindex=-1";
        $query = knjl415hQuery::getNameMst($model->examYear, "L003");
        $arg["TOP"]["APPLICANTDIV"] = makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        if ($model->field["APPLICANTDIV"] == "2") {
            $arg["dispNyusiH"] = "1";
        } else {
            $arg["dispNyusiJ"] = "1";
        }

        //受験コースコンボ
        $model->field["EXAMCOURSECD"] = ($model->cmd == "changeApp") ? "": $model->field["EXAMCOURSECD"];
        $query = knjl415hQuery::getExamCourseMst($model);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $arg["TOP"]["EXAMCOURSECD"] = makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->field["EXAMCOURSECD"], $extra, 1, "");

        //模試得点
        $extra = "onchange=\"chkEnableChgDisp(this, 'now')\";";
        $arg["TOP"]["MOCK_LINE"] = knjCreateTextBox($objForm, $model->field["MOCK_LINE"], "MOCK_LINE", 3, 3, $extra);

        //コース名取得
        $examCourseMst = array();
        $query = knjl415hQuery::getExamCourseMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examCourseMst[$row["KEY"]] = $row["NAME"];
        }

        $syutuganMst = array();
        $query = knjl415hQuery::getEntexamSettingMst($model, "L006");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $syutuganMst[$row["VALUE"]] = $row["NAME"];
        }

        //一覧表示
        $model->arr_examno = array();
        if ($model->s_receptno != "" && $model->field["APPLICANTDIV"] != "" && $model->field["EXAMCOURSECD"] != "") {
            //データ取得
            $result = $db->query(knjl415hQuery::selectQuery($model));
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $saisyuuArry = array();
                //HIDDENに保持する用
                $examno = $row["EXAMNO"];
                $model->arr_examno[] = $examno;

                //内申内諾コース
                addSaisyuuCd($saisyuuArry, "1", $row["CD005_REMARK1"], $row["CD005_REMARK2"]);
                $row["CD005_REMARK1"] = $examCourseMst[$row["CD005_REMARK1"]].($row["CD005_REMARK2"] != null ? "<BR>".$syutuganMst[$row["CD005_REMARK2"]] : "");
                //強化クラブ内諾コース
                addSaisyuuCd($saisyuuArry, "2", $row["CD005_REMARK5"], $row["CD005_REMARK6"]);
                $row["CD005_REMARK5"] = $examCourseMst[$row["CD005_REMARK5"]].($row["CD005_REMARK6"] != null ? "<BR>".$syutuganMst[$row["CD005_REMARK6"]] : "");

                //模試名
                $extra = " style=\"width:110px;\" disabled ";
                $query = knjl415hQuery::getEntexamSettingMst($model, "L100", $row["CD003_REMARK1"]);
                $subrow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row["CD003_REMARK1"] = $subrow["LABEL"];
                //模試成績
                $extra = "style=\"text-align:right;background-color:lightgray\" readonly";
                $row["CD003_REMARK2"] = $row["CD003_REMARK2"];
                //模試内諾コース
                addSaisyuuCd($saisyuuArry, "3", $row["CD003_REMARK10"], $row["CD003_REMARK11"]);
                $row["CD003_REMARK10"] = $examCourseMst[$row["CD003_REMARK10"]].($row["CD003_REMARK11"] != null ? "<BR>".$syutuganMst[$row["CD003_REMARK11"]] : "");

                //OPT判定
                $extra = " style=\"width:80px;\" disabled ";
                $query = knjl415hQuery::getEntexamSettingMst($model, "L101", $row["CD004_REMARK4"]);
                $subrow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row["CD004_REMARK4"] = $subrow["LABEL"];
                //OPT内諾コース
                addSaisyuuCd($saisyuuArry, "4", $row["CD004_REMARK10"], $row["CD004_REMARK11"]);
                $row["CD004_REMARK10"] = $examCourseMst[$row["CD004_REMARK10"]].($row["CD004_REMARK11"] != null ? "<BR>".$syutuganMst[$row["CD004_REMARK11"]] : "");
                if ($row["CD006_REMARK10"] == "" && $row["CD006_REMARK11"] == "" && get_count($saisyuuArry) > 0) {
                    asort($saisyuuArry);
                    $sCd = "";
                    $sIdx = "";
                    foreach ($saisyuuArry as $saisyuuIdx => $saisyuuCd) {
                        $sCd = $saisyuuCd;
                        $sIdx = $saisyuuIdx;
                        break;
                    }
                    if ($row["CD006_REMARK12"] == "") {
                        $row["CD006_REMARK12"] = $saisyuuIdx;
                    }
                    $cutsCd = explode('-', $sCd);
                    $row["CD006_REMARK10"] = $cutsCd[0];
                    $row["CD006_REMARK11"] = $cutsCd[1];
                }
                //最終内諾内諾コース
                $extra = " style=\"width:190px;\" onchange='chgChk(this);' ";
                $query = knjl415hQuery::getExamCourseMst($model);
                $value = (isset($model->warning)) ? $model->examData[$examno]["CD006_REMARK10"] : $row["CD006_REMARK10"];
                $row["CD006_REMARK10"] = makeCmb($objForm, $arg, $db, $query, "CD006_REMARK10"."-".$examno, $value, $extra, 1, "BLANK");
                //最終内諾出願区分
                $extra = " style=\"width:80px;\"  onchange='chgChk(this);' ";
                $query = knjl415hQuery::getEntexamSettingMst($model, "L006");
                $value = (isset($model->warning)) ? $model->examData[$examno]["CD006_REMARK11"] : $row["CD006_REMARK11"];
                $row["CD006_REMARK11"] =  makeCmb($objForm, $arg, $db, $query, "CD006_REMARK11"."-".$examno, $row["CD006_REMARK11"], $extra, 1, "BLANK");
                //最終内諾出願区分
                $extra = " style=\"width:120px;\"  onchange='chgChk(this);' ";
                $query = knjl415hQuery::getEntexamSettingMst($model, "L102");
                $value = (isset($model->warning)) ? $model->examData[$examno]["CD006_REMARK12"] : $row["CD006_REMARK12"];
                $row["CD006_REMARK12"] =  makeCmb($objForm, $arg, $db, $query, "CD006_REMARK12"."-".$examno, $row["CD006_REMARK12"], $extra, 1, "BLANK");

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
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->s_examOrg, "S_EXAMNO", 7, 7, $extra);
        //次へボタン
        $extra = "onclick=\"return btn_submit('next');\"";
        $arg["BUTTON"]["NEXT"] = knjCreateBtn($objForm, "btn_next", ">>", $extra);
        
        //前へボタン
        $extra = "onclick=\"return btn_submit('back');\"";
        $arg["BUTTON"]["BACK"] = knjCreateBtn($objForm, "btn_back", "<<", $extra);


        //ボタン作成
        makeBtn($objForm, $arg, $dataflg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COUNT", $count);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL415H");
        knjCreateHidden($objForm, "CHGFLG", "0");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl415hForm1.html", $arg);
    }
}

function addSaisyuuCd(&$saisyuuArry, $id, $exCourseCd, $shDiv)
{
    if ($exCourseCd != "" && $shDiv != "") {
        $saisyuuArry[$id] = $exCourseCd.'-'.$shDiv;
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "    ", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
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
function makeBtn(&$objForm, &$arg, $dataflg)
{
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
