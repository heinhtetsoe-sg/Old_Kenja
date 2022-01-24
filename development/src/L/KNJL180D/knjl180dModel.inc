<?php

require_once('for_php7.php');

class knjl180dModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $ObjYear;
    var $remark1;
    var $remark2;
    var $remark3;

    function knjl180dModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $this->ObjYear      = VARS::post("YEAR");
        $this->applicantdiv = "1";  //固定値
        $this->exam_type    = "1";  //固定値

        $this->field = array(
                "HEADER" => VARS::post("HEADER"),
        );

        $this->cmd = VARS::request("cmd");

        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->hid_examno       = VARS::post("HID_EXAMNO");

        $this->updateDataArry();

        $array = explode(",", $this->hid_examno);
        foreach ($array as $key => $examno) {
            if (VARS::post("ENTRYFLG_".$examno)) {
                if (VARS::post("ENTRYCANCEL_".$examno)) {
                    $this->field[$examno]["ENTDIV"]          = "2";
                } else {
                    $this->field[$examno]["ENTDIV"]          = "1";
                }
            } else {
                $this->field[$examno]["ENTDIV"]          = "";
            }

            //振込日データ
            if (VARS::post("PROCEDUREDATE_".$examno)) {
                $this->field[$examno]["PROCEDUREDATE"] = VARS::post("PROCEDUREDATE_".$examno);
            }
        }

        $db = Query::dbCheckOut();
        $this->remark1 = $db->getOne(knjl180dQuery::getNameMstOne($this->ObjYear, "L012", "1", "ABBV3"));
        $this->remark2 = $db->getOne(knjl180dQuery::getNameMstOne($this->ObjYear, "L012", "1", "NAMESPARE1"));
        $this->remark3 = $db->getOne(knjl180dQuery::getNameMstOne($this->ObjYear, "L012", "1", "NAMESPARE2"));
        Query::dbCheckIn($db);

    }

    //日付変換 項目チェック後に行う。
    function date_rep($data) {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if ($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

    function &updateDataArry() {
        $this->data = array();
        $array = explode(",", $this->hid_examno);
        foreach ($array as $key => $examno) {
            if (VARS::post("ENTRYFLG_".$examno)) {
                if (VARS::post("ENTRYCANCEL_".$examno)) {
                    $this->data["ENTDIV"][$examno]          = "2";
                } else {
                    $this->data["ENTDIV"][$examno]          = "1";
                }
            } else {
                $this->data["ENTDIV"][$examno]          = "";
            }

            //振込日データ
            if (VARS::post("PROCEDUREDATE_".$examno)) {
                $this->data["PROCEDUREDATE"][$examno] = VARS::post("PROCEDUREDATE_".$examno);
            }
        }
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();
        $header =  array();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**************/
        /*  ヘッダー  */
        /**************/
        
        $header["YEAR"]                = "入試年度";
        $header["TESTDIV"]             = "入試区分";
        $header["EXAMNO"]              = "受験番号";
        $header["NAME"]                = "氏名";
        $header["PROCEDUREDATE"]       = "振込日";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/

            $query = knjl180dQuery::selectMainQuery1($this);
            $TestdivNameCd2 = $db->getOne(knjl180dQuery::getNameMst($this->ObjYear, "L004", $this->testdiv));
            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                    $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                    //入試年度
                    $row["YEAR"] = $this->ObjYear;
                    //振込日
                    $row["TESTDIV"] = $TestdivNameCd2;
                    //振込日
                    $row["PROCEDUREDATE"] = str_replace('-', '/', $row["PROCEDUREDATE"]);

                    $array = $header;

                    //ヘッダ順に値をセット
                    $sep = "";
                    foreach ($array as $field => $label) {
                        $contents .= $sep.$row[$field];
                        $sep = ",";
                    }
                    $contents .= "\n";

                    $header_data = true;
                }
                $result->free();
            }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/

        //ファイル名
        $title = $this->ObjYear.'年度入試 '.$TestdivName.'データ';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
            $this->updateDataArry();
            //更新
            knjl180dQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
    }
}
?>
