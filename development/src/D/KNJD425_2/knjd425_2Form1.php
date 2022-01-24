<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425_2Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        // Add by HPA textarea_cursor 2020-02-03 start
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJD425_2Form1_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJD425_2Form1_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJD425_2Form1_CurrentCursor\");</script>";
            $model->message915 = "";
        }
        // Add by HPA for textarea_cursor 2020/02/20 end

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjd425_2index.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //更新日(プロパティが立っているときのみ表示)
        if ($model->Properties["useKnjd425DispUpdDate"] == "1") {
            $arg["data"]["UPDDATE"] = "更新日:".$model->upddate;
        }

        //所感タイトル
        $db = Query::dbCheckOut();
        //$query = knjd425_2Query::getGuidanceKindName($model, $model->selKindNo);
        //$result = $db->query($query);
        $result = $this->loadGuidanceKindName($db, $model, $model->selKindNo);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $arg["data"]["TITLE"] = $row["BTN_SUBFORMCALL"];
        }

        // Add by HPA for title start 2020/02/03
        $nameShow = $arg["data"]["NAME_SHOW"];
        $data_title = $arg["data"]["TITLE"];
        $htmlTitle = "".$nameShow."の".$data_title."の情報画面";
        echo "<script>
        var title= '".$htmlTitle."';
        </script>";
        // Add by HPA for title end 2020/02/20

        //入力項目テキストボックス
        //データ取得
        $outcnt = "1";
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

        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $extra = "id=\"REMARK_0_0\" aria-label= \"".$arg["data"]["TITLE"]." 全角44文字X25行まで\"";
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
        $wkarry = $recarry[0];
        $val = $wkarry["REMARK1"];
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK_0_0", 10, 88, "", $extra, $val);

        knjCreateHidden($objForm, "REMARK_0_0_KETA", 88);
        knjCreateHidden($objForm, "REMARK_0_0_GYO", 25);
        KnjCreateHidden($objForm, "REMARK_0_0_STAT", "statusarea1");

        $arg["data"]["MERGECEL"] = "1";

        //終了ボタン
        $link = REQUESTROOT."/D/KNJD425/knjd425index.php?cmd=edit&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $extra = " aria-label = \"戻る\" onclick=\"window.open('$link','_self');\"";
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //更新ボタンを作成
        /* Edit by HPA for current_cursor start 2020/02/03 */
        $extra = "id = \"update2\" onclick=\"current_cursor('update2');return btn_submit('update2');\"";
        /* Edit by HPA for current_cursor end 2020/02/20 */
        $arg["btn_update2"] = KnjCreateBtn($objForm, "btn_update2", "更新", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd425_2Form1.html", $arg);
    }

    function getDataList($db, $model, $outcnt) {
        $retlist = array();
        $query = knjd425_2Query::chkDataExist($model);
        $chkcnt = $db->getOne($query);
        if ($chkcnt > 0) {
            for ($ii = 1;$ii <= $outcnt;$ii++) {
                $query = knjd425_2Query::getDataList($model, $ii);
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

    //項目名称取得
    function loadGuidanceKindName($db, $model, $kno="")
    {
        //データの優先度として、個人>年組>年度となる。データが無ければ下位SQLでデータを取得していく。
        $result = array();
        $specifyschregflg = "";
        $query = knjd425_2Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->getRow($query);
        if (get_count($result) > 0) {
            //if文を抜けて最後の処理を実施。
        } else {
            //データが無い時は学籍番号抜きで取り直し。
            $specifyschregflg = "1";
            $query = knjd425_2Query::getGuidanceKindName($model, $specifyschregflg, $kno);
            $result = $db->getRow($query);
            if (get_count($result) > 0) {
                //if文を抜けて最後の処理を実施。
            } else {
                //データが無い時は年組、学籍番号抜きで取り直し。
                $specifyschregflg = "2";
            }
        }
        $query = knjd425_2Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->query($query);
        return $result;
    }
}
?>
