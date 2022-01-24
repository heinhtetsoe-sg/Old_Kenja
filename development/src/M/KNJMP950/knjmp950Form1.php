<?php

require_once('for_php7.php');

class knjmp950Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjmp950Form1", "POST", "knjmp950index.php", "", "knjmp950Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjmp950Query::getYear();
        makeCombo($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "", $model);

        //予算区分(01:収入、03:支出)
        if ($model->cmd === 'data_set') {
            if ($model->getYosanDiv === '01' || $model->getYosanDiv === '02') {
                //収入
                $model->field["SET_DIV"] = '01';
            } else {
                //支出
                $model->field["SET_DIV"] = '03';
            }
        }
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjmp950Query::getYosanSetDiv();
        makeCombo($objForm, $arg, $db, $query, $model->field["SET_DIV"], "SET_DIV", $extra, 1, "", $model);
        
        //予算科目
        if ($model->cmd === 'data_set') {
            $model->field["YOSAN_L_CD"] = $model->getYosanLcd;
        }
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjmp950Query::getLevyLDiv($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["YOSAN_L_CD"], "YOSAN_L_CD", $extra, 1, "BLANK", $model);

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "edit") {
            unset($model->getYear);
            unset($model->getYosanDiv);
            unset($model->getYosanLcd);
            unset($model->getYosanLMcd);
            unset($model->getRequestNo);
            $model->field["YOSAN_L_M_CD"] = "";
            $model->field["REQUEST_DATE"] = "";
            $model->field["REQUEST_GK"] = "";
            $model->field["BOFORE_REQUEST_GK"] = "";
            $model->field["REQUEST_SAGAKU"] = "";
            $model->field["REQUEST_REASON"] = "";
        }
        if ($model->cmd == "data_set"){
            if (!isset($model->warning)){
                $Row = $db->getRow(knjmp950Query::getYosanData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        
        //管理番号をセット
        $model->getRequestNo = $Row["REQUEST_NO"];
        
        //作成日
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$Row["REQUEST_DATE"]),"");
        
        //予算項目
        $extra = "";
        $query = knjmp950Query::getLevyMDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["YOSAN_L_M_CD"], "YOSAN_L_M_CD", $extra, 1, "BLANK", $model);
        
        //予算額
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);keisanZangk(this.value);\"";
        $arg["data"]["REQUEST_GK"] = knjCreateTextBox($objForm, $Row["REQUEST_GK"], "REQUEST_GK", 7, 7, $extra);
        
        //前年度予算額
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["BOFORE_REQUEST_GK"] = knjCreateTextBox($objForm, $Row["BOFORE_REQUEST_GK"], "BOFORE_REQUEST_GK", 7, 7, $extra);
        
        //増減
        $setSagaku = $Row["REQUEST_GK"] - $Row["BOFORE_REQUEST_GK"];
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["REQUEST_SAGAKU"] = knjCreateTextBox($objForm, $setSagaku, "REQUEST_SAGAKU", 7, 7, $extra);

        //摘要
        $extra = " STYLE=\"ime-mode: active;text-align:\"";
        $arg["data"]["REQUEST_REASON"] = knjCreateTextBox($objForm, $Row["REQUEST_REASON"], "REQUEST_REASON", 60, 120, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjmp950Form1.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;

    $query = knjmp950Query::getList($model);
    $result = $db->query($query);
    $sumRequestGk = "";
    $sumBeforeRequestGk = "";
    $sumRequestSagaku = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowlist["LEVY_M_NAME"] = $db->getOne(knjmp950Query::getLevyMDivName($model, $rowlist["YOSAN_L_CD"], $rowlist["YOSAN_M_CD"]));
        if ($rowlist["YOSAN_DIV"] === '02') {
            $rowlist["LEVY_M_NAME"] = '雑収入';
        } else if ($rowlist["YOSAN_DIV"] === '04') {
            $rowlist["LEVY_M_NAME"] = '予備費';
        }
        //増減
        $RequestSagaku = $rowlist["REQUEST_GK"] - $rowlist["BOFORE_REQUEST_GK"];
        $rowlist["REQUEST_SAGAKU"] = $RequestSagaku;
        
        //合計用
        $sumRequestGk += $rowlist["REQUEST_GK"];
        $sumBeforeRequestGk += $rowlist["BOFORE_REQUEST_GK"];
        $sumRequestSagaku += $RequestSagaku;
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    
    //合計
    $arg["sumdata"]["REQUEST_GK"] = $sumRequestGk;
    $arg["sumdata"]["BOFORE_REQUEST_GK"] = $sumBeforeRequestGk;
    $arg["sumdata"]["REQUEST_SAGAKU"] = $sumRequestSagaku;
    
    return $retCnt;
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name != "YEAR") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = " onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
