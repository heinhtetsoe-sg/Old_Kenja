<?php

require_once('for_php7.php');

class knjl221gModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $field = array();   //フィールドデータ

    function knjl221gModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;

        //フィールド
        $this->field = array("CONFIDENTIAL_RPT01"   =>  VARS::post("CONFIDENTIAL_RPT01"),       //国語
                             "CONFIDENTIAL_RPT02"   =>  VARS::post("CONFIDENTIAL_RPT02"),       //社会
                             "CONFIDENTIAL_RPT03"   =>  VARS::post("CONFIDENTIAL_RPT03"),       //数学
                             "CONFIDENTIAL_RPT04"   =>  VARS::post("CONFIDENTIAL_RPT04"),       //理科
                             "CONFIDENTIAL_RPT05"   =>  VARS::post("CONFIDENTIAL_RPT05"),       //音楽
                             "CONFIDENTIAL_RPT06"   =>  VARS::post("CONFIDENTIAL_RPT06"),       //美術
                             "CONFIDENTIAL_RPT07"   =>  VARS::post("CONFIDENTIAL_RPT07"),       //保険
                             "CONFIDENTIAL_RPT08"   =>  VARS::post("CONFIDENTIAL_RPT08"),       //技術
                             "CONFIDENTIAL_RPT09"   =>  VARS::post("CONFIDENTIAL_RPT09"),       //外国
                             "TOTAL5"               =>  VARS::post("TOTAL5"),                   //5科目
                             "TOTAL_ALL"            =>  VARS::post("TOTAL_ALL"),                //9教科
                             //欠席の記録
                             "ABSENCE_DAYS3"        =>  VARS::post("ABSENCE_DAYS3"),            //欠席日数(3年)

                             "TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),           //クラブ活動
                             "REMARK1_004"          =>  VARS::post("REMARK1_004"),              //入試相談日
                             "REMARK2_004"          =>  VARS::post("REMARK2_004"),              //相談結果
                             "REMARK3_004"          =>  VARS::post("REMARK3_004"),              //体験ゼミ結果
                             "REMARK10_031"         =>  VARS::post("REMARK10_031"),             //特記事項
                             "REMARK6_031"          =>  VARS::post("REMARK6_031"),              //面接日
                             "REMARK7_031"          =>  VARS::post("REMARK7_031"),              //開始時間(時)
                             "REMARK8_031"          =>  VARS::post("REMARK8_031"),              //開始時間(分)
                             "REMARK9_031"          =>  VARS::post("REMARK9_031")               //面接会場
                            );

        //DB接続
        $db = Query::dbCheckOut();

        //各項目の教科名称取得
        $this->l008Arr = array();
        $this->total5Flg = $sep = '';
        $query = knjl221gQuery::get_name_cd($this->year, "L008");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->l008Arr[$row["VALUE"]] = $row["ABBV1"];
            $setNameSpare1 = $row["NAMESPARE1"] == '1' ? '1': '0';
            $this->total5Flg .= $sep.$row["VALUE"].'-'.$setNameSpare1;
            $sep = ':';
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
        if (strlen($this->field["TOTALSTUDYTIME"]) > 30) {
            $this->setWarning("MSG915","クラブ活動は30バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2_004"]) > 12) {
            $this->setWarning("MSG915","相談結果は12バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK3_004"]) > 12) {
            $this->setWarning("MSG915","体験ゼミ結果は12バイトまでです。");
            return false;
        }
        /* 特記事項 */
        if ($this->validate_row_cnt($this->field["REMARK10_031"], (12 * 2)) > 6) {
            $this->setWarning("MSG915","(特記事項は6行までです)");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl221gQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

}
?>