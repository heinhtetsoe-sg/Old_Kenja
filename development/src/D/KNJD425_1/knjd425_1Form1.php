<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425_1Form1
{
    function main(&$model)
    {
        $objForm = new form;

        // Add by HPA textarea_cursor 2020-02-03 start
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJD425_1Form1_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJD425_1Form1_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJD425_1Form1_CurrentCursor\");</script>";
            $model->message915 = "";
        }
        // Add by HPA for textarea_cursor 2020-02-20 end
        $arg = array();
        //フォーム作成
         /* Add by HPA for PC-talker 読み start 2020/02/03 */
            $arg["SNAME"] = "".$model->schregno."".$model->name."の情報画面";
            /* Add by HPA for PC-talker 読み end 2020/02/20 */
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knjd425_1index.php", "", "subform1");

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //更新日(プロパティが立っているときのみ表示)
        if ($model->Properties["useKnjd425DispUpdDate"] == "1") {
            $arg["data"]["UPDDATE"] = "更新日:".$model->upddate;
        }

        //所感タイトル
        $db = Query::dbCheckOut();
        //$query = knjd425_1Query::getGuidanceKindName($model, $model->selKindNo);
        //$result = $db->query($query);
        $result = $this->loadGuidanceKindName($db, $model, $model->selKindNo);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["TITLE"] = $row["BTN_SUBFORMCALL"];
        }
        $result->free();

        // Add by HPA for title start 2020/02/03
        $nameShow = $arg["data"]["NAME_SHOW"];
        $data_title = $arg["data"]["TITLE"];
        $htmlTitle = "".$nameShow."の".$data_title."の情報画面";
        echo "<script>
        var title= '".$htmlTitle."';
        </script>";
        // Add by HPA for title end 2020/02/20

        //入力項目件数を固定で設定。
        //出力するタイトルを固定にする。
        $ttlarry = array("障害種別", "作成日", "作成者");
        $outcnt = get_count($ttlarry);

        //データ取得
        if (get_count($model->remarkarry) < 1) {
            //未入力なら、DBから取得。
            $recarry = array();
            $recarry[] = $this->getDataList($db, $model, $outcnt);
        } else {
            //入力中なら、ここ。
            $addwk = array();
            for ($dcnt = 0;$dcnt < get_count($model->remarkarry);$dcnt++) {
                $addwk["REMARK".($dcnt % $outcnt+1)] = $model->remarkarry[$dcnt];
                if (get_count($model->remarkarry) == 1) {
                        $recarry[] = $addwk;
                        $addwk = array();
                } else {
                    if ($dcnt % $outcnt == $outcnt-1) {
                        $recarry[] = $addwk;
                        $addwk = array();
                    }
                }
            }
        }

        //入力項目を設定
        for ($i = 1;$i <= get_count($ttlarry);$i++) {
            $setbuf = array();
            $setbuf["REMARKTITLE"] = $ttlarry[($i-1)];
            $wkarry = $recarry[0];
            $val = $wkarry["REMARK".$i];
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $extra = "aria-label = \"{$ttlarry[$i-1]}\"";
            /* Edit by HPA for PC-talker 読み end 2020/02/20 */
            if ($i == 1) {
                $query = knjd425_1Query::getNameMst($model, "D091");
                $setbuf["REMARK"] = $this->makeCmb($objForm, $arg, $db, $query, "REMARK_0_".($i-1), $val, $extra, 1);
            } else if ($i == 2) {
                if ($val == "") {
                    $val = CTRL_DATE;
                }
                $param = "";
                /* Edit by HPA for current_cursor start 2020/02/03 */
                $setbuf["REMARK"] = View::popUpCalendar($objForm, "REMARK_0_".($i-1), str_replace("-", "/", $val), $param , "作成日");
                /* Edit by HPA for current_cursor end 2020/02/20 */
            } else if ($i == 3) {
                $query = knjd425_1Query::getStaffList($model);
                $setbuf["REMARK"] = $this->makeCmb($objForm, $arg, $db, $query, "REMARK_0_".($i-1), $val, $extra, 1);
            }
            $arg["list"][] = $setbuf;
        }

        Query::dbCheckIn($db);

        //終了ボタンを作成する
        $link = REQUESTROOT."/D/KNJD425/knjd425index.php?cmd=edit&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //更新ボタンを作成
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
        $extra = "id = \"update1\" onclick=\"current_cursor('update1');return btn_submit('update1');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
        $arg["btn_update1"] = KnjCreateBtn($objForm, "btn_update1", "更新", $extra);

        //hidden
        $nx = 1;
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COLCNT", $outcnt);
        knjCreateHidden($objForm, "HID_ROWCNT", "1");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd425_1Form1.html", $arg);
    }

    //データ取得
    function getDataList($db, $model, $outcnt) {
        $retlist = array();
        $query = knjd425_1Query::chkDataExist($model);
        $chkcnt = $db->getOne($query);
        if ($chkcnt > 0) {
            for ($ii = 1;$ii <= $outcnt;$ii++) {
                $query = knjd425_1Query::getDataList($model, $ii);
                $result = $db->query($query);
                $getval = "";
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["REMARK"] !== null && $row["REMARK"] !== "") {
                        $getval = $row["REMARK"];
                    }
                }
                $retlist["REMARK".$ii] = $getval;
            }
        }
        return $retlist;
    }

    //コンボ作成
    function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
    {
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $result->free();

        if ($name == "REMARK_0_2") {
            $value = ($value && $value_flg) ? $value : STAFFCD;
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }

        return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }

    //項目名称取得
    function loadGuidanceKindName($db, $model, $kno="")
    {
        //データの優先度として、個人>年組>年度となる。データが無ければ下位SQLでデータを取得していく。
        $specifyschregflg = "";
        $query = knjd425_1Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->getRow($query);
        if (get_count($result) > 0) {
            //if文を抜けて最後の処理を実施。
        } else {
            //データが無い時は学籍番号抜きで取り直し。
            $specifyschregflg = "1";
            $query = knjd425_1Query::getGuidanceKindName($model, $specifyschregflg, $kno);
            $result = $db->getRow($query);
            if (get_count($result) > 0) {
                //if文を抜けて最後の処理を実施。
            } else {
                //データが無い時は年組、学籍番号抜きで取り直し。
                $specifyschregflg = "2";
            }
        }
        $query = knjd425_1Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->query($query);
        return $result;
    }
}
?>
