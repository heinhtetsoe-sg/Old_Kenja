<?php

require_once('for_php7.php');

class knje015Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje015index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //2018年度以降かどうか
        $model->over2018 = "";
        if ($model->exp_year < 2018) {
            $model->over2018 = "";
        } elseif ($model->exp_year < 2020) {
            $isAny = false;
            $query = knje015Query::getTotalRemarkDisable($model);
            $result = $db->query($query);
            while ($disableRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($disableRow["YEAR"] == $model->exp_year
                    && $disableRow["GRADE"] == $model->exp_grade) {
                    $isAny = true;
                    break;
                }
            }
            $result->free();
            //取得した年度のGRADEは使用不可
            if ($isAny) {
                $model->over2018 = "1";
            }
        } else {
            // 2020年度以降は全て使用不可
            $model->over2018 = "1";
        }

        $disabled = "";
        $grayFlg = false; //テキストエリア更新不可状態（グレー）のフラグ
        if ($model->getPrgId) {
            //コールされた場合はプログラム使用可
        } elseif ($model->schregno && $model->over2018 != "1") {
//            $arg["err_alert"] = "alert('更新対象外の生徒です。');";
//            $disabled = " disabled ";
        }

        //対象年度において生徒の担任でないなら参照不可（グレー）
        $isTaninCnt = $db->getOne(knje015Query::getTaninCnt($model));
        if (AUTHORITY != DEF_UPDATABLE && $model->Properties["KNJE015_TantouIgaiFuka"] == "1" && $isTaninCnt == 0) {
            $grayFlg = true;
            $disabled = " disabled ";
        }

        //生徒情報表示
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        /**********/
        /* コンボ */
        /**********/
        //学年
        $query = knje015Query::getGrade($model->gradeYear, $model->schregno);
        $extra = "onChange=\"return btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_YEAR", $model->gradeYear, $extra, 1, "");

        /******************/
        /* テキストエリア */
        /******************/
        makeHexamEntRemarkDat($objForm, $arg, $db, $model, $grayFlg);

        /****************/
        /* 項目名セット */
        /****************/
        if ($model->Properties["HEXAM_ENTREMARK_LEARNING_DAT__REMARK"] != '') {
            $arg["REMARK_TITLE"] = $model->Properties["HEXAM_ENTREMARK_LEARNING_DAT__REMARK"];
        } else {
            $arg["REMARK_TITLE"] = '備考';
        }

        /**********/
        /* ボタン */
        /**********/

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $setSubwin = "SUBWIN2";
        if (!$model->getPrgId) {
            $extra = "onclick=\"closeWin();\"";
            $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
        } else {
            $extra = "onclick=\"btn_openerSubmit();\"";
            $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
            $setSubwin = "SUBWIN3";
        }

        //CSVボタン
        $extra = "onClick=\"wopen('".REQUESTROOT."/X/KNJX_E015/knjx_e015index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE015&SEND_AUTH={$model->auth}&SELECT_SCHKIND={$model->selectSchoolKind}&GRADE_HR_CLASS={$model->exp_grade}{$model->exp_hr_class}','{$setSubwin}',0,0,window.outerWidth, window.outerHeight);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "データCSV", $extra.$disabled);

        //部活動選択ボタン
        $arg["btn_club_tra3"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SEQ003", $disabled);

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            //指導上参考となる諸事項
            $arg["btn_club_kirokubikou_tra3"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "SEQ003", $disabled);
            $arg["btn_club_kirokubikou_tra5"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "SEQ005", $disabled);
        }

        //検定選択ボタン
        $arg["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "SEQ004", $disabled);

        //賞選択ボタン
        if ($model->Properties["useHyosyoSansyoButton_H"]) {
            $arg["btn_hyosyo_tra5"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "SEQ005", $disabled);
        }

        if ($model->Properties["Sansho_Botton_Hyouji"] == 1) {
            $extra = " onclick=\"if(!btn_check()) loadwindow('knje015index.php?cmd=tuutihyou',0,0,750,450);\"";
            $arg["btn_tuutihyou"] = knjCreateBtn($objForm, "tuutihyou", "通知票所見参照", $extra);

            //資格参照
            $extra = " onclick=\"if(!btn_check()) loadwindow('knje015index.php?cmd=sikaku',0,0,700,350);\"";
            $arg["btn_committee"] = knjCreateBtn($objForm, "sikaku", "資格参照", $extra);

            //部活動参照ボタンを作成する
            $extra = " onclick=\"if(!btn_check()) loadwindow('knje015index.php?cmd=bukatu',0,0,650,350);\"";
            $arg["btn_club"] = KnjCreateBtn($objForm, "bukatu", "部活動参照", $extra);
        }


        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje015Form1.html", $arg);
    }
}

function makeHexamEntRemarkDat(&$objForm, &$arg, $db, &$model, $grayFlg)
{
    if (!isset($model->warning)) {
        $query = knje015Query::getMainData($model);
        $result = $db->query($query);
        $rec = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rec['YEAR'] = $row['YEAR'];
            $rec['SCHREGNO'] = $row['SCHREGNO'];
            $rec['SEQ' . $row['TRAIN_SEQ']] = $row['REMARK'];
        }
        //総合所見取得
        $query = knje015Query::getMainDataTotalremark($model);
        $rec['TOTALREMARK'] = $db->getOne($query);
        //まなびの記録取得
        $query = knje015Query::getMainDataManabi($model);
        $rec['REMARK'] = $db->getOne($query);

        $arg["NOT_WARNING"] = 1;
    } else {
        $rec = $model->field;
    }

    for ($i = 1; $i <= 6; $i++) {
        $height = (int)$model->gyou * 13.5 + ((int)$model->gyou -1) * 3 + 5;

        $extra = "style=\"height:{$height}px;\" ";
        if ($grayFlg) {
            $extra = "style=\"height:{$height}px; background-color:gray;\" ";
        }
        $arg['SEQ00' . $i] = KnjCreateTextArea($objForm, 'SEQ00' . $i, ((int)$model->gyou + 1), ((int)$model->moji * 2 + 1), 'soft', $extra, $rec['SEQ00' . $i]);
    }
    $arg["SEQ_COMMENT"]    = "(全角{$model->moji}文字{$model->gyou}行まで)";

    //総合所見
    $height = (int)$model->totalremark_gyou * 13.5 + ((int)$model->totalremark_gyou -1) * 3 + 5;
    $arg["SOUGOU_HEIGHT"] = $height;
    $extra = "style=\"height:{$height}px;\" ";

    if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == "1" || ($model->Properties["useSeitoSidoYorokuSougouShoken"] != "1" && $model->Properties["useSeitoSidoYorokuSougouShoken"] != "2" || $grayFlg)) {
        $extra = "style=\"height:{$height}px; background-color:gray;\" ";
    }

    $arg["TOTALREMARK"] = KnjCreateTextArea($objForm, 'TOTALREMARK', ((int)$model->totalremark_gyou + 1), ((int)$model->totalremark_moji * 2 + 1), 'soft', $extra, $rec['TOTALREMARK']);
    $arg["TOTALREMARK_COMMENT"] = "(全角{$model->totalremark_moji}文字{$model->totalremark_gyou}行まで)";

    //まなびの記録
    if ("1" != $model->Properties["unuse_KNJE015_HEXAM_ENTREMARK_LEARNING_DAT_REMARK"]) {
        $height = (int)$model->remark_gyou * 13.5 + ((int)$model->remark_gyou -1) * 3 + 5;
        $extra = "style=\"height:{$height}px;\" ";
        if ($grayFlg) {
            $extra = "style=\"height:{$height}px; background-color:gray;\" ";
        }
        $arg["REMARK"] = KnjCreateTextArea($objForm, 'REMARK', ((int)$model->remark_gyou + 1), ((int)$model->remark_moji * 2 + 1), 'soft', $extra, $rec['REMARK']);
        $arg["REMARK_COMMENT"] = "(全角{$model->remark_moji}文字{$model->remark_gyou}行まで)";
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        $extra = "";
        if ($div == "club") {       //部活動
            $tgt = "/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php";
            $w = 800;
            $h = 350;
        } elseif ($div == "qualified") {       //検定
            $tgt = "/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php";
            $w = 900;
            $h = 500;
        } elseif ($div == "hyosyo") {          //賞
            $tgt = "/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php";
            $w = 600;
            $h = 350;
        } elseif ($div == "kirokubikou") {     //記録備考
            $tgt = "/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php";
            $w = 800;
            $h = 350;
        }
        if ($extra == "") {
            $extra = $disabled." onclick=\"if(!btn_check()) loadwindow('".REQUESTROOT.$tgt."?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->gradeYear}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$w},{$h});\"";
        }

        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
