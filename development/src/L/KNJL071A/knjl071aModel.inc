<?php

require_once('for_php7.php');

class knjl071aModel extends model
{
    public $cmd;
    public $ObjYear;
    public $examType;
    public $applicantdiv;
    public $testdiv;
    public $sort;
    public $hid_ReceptNo;
    public $syussinSchool;
    public $sibouCourse;
    public $arrInput;

    public function knjl071aModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->hid_ReceptNo);
        }

        $this->ObjYear  = CTRL_YEAR+1;
        $this->examType = 1;    //固定

        $this->applicantdiv     = VARS::post("APPLICANTDIV");
        $this->testdiv          = VARS::post("TESTDIV");
        $this->sort             = VARS::post("SORT");
        $this->syussinSchool    = VARS::post("SYUSSIN_SCHOOL");
        $this->sibouCourse      = VARS::post("SIBOU_COURSE");

        $this->hid_ReceptNo = VARS::post("HID_RECEPTNO");

        $this->cmd = VARS::request("cmd");

        $this->arrInput = array();
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //初期化
        $this->setReceptNo = $this->arrExamNo = $this->arrInput = array();

        $db = Query::dbCheckOut();
        $arrTokutaiRank = array();
        //特待の優先順位を取得
        $result = $db->query(knjl071aQuery::getHonordivMstPriority($this));
        $cnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrTokutaiRank[$cnt]["HONORDIV"] = $row["HONORDIV"];
            $cnt++;
        }
        $result->free();
        Query::dbCheckIn($db);

        //再配置
        if ($this->cmd == 'update') {
            $this->setReceptNo = explode(",", $this->hid_ReceptNo);
        } else {
            $this->setReceptNo = $this->aryAllRecept;
        }
        $this->setReceptNo = array_unique($this->setReceptNo);
        foreach ($this->setReceptNo as $key => $receptno) {
            $this->arrExamNo[$receptno]                   = VARS::post("EXAMNO-".$receptno);
            $this->arrInput[$receptno]["KAKUTEI_TOKUTAI"] = VARS::post("KAKUTEI_TOKUTAI-".$receptno);
            $this->arrInput[$receptno]["JIZEN_TOKUTAI"]   = VARS::post("JIZEN_TOKUTAI-".$receptno);
            $this->arrInput[$receptno]["TOKUTAI_SINSEI"]  = VARS::post("TOKUTAI_SINSEI-".$receptno);
            $this->arrInput[$receptno]["SIKAKU_KATSUYO"]  = VARS::post("SIKAKU_KATSUYO-".$receptno);
            $this->arrInput[$receptno]["DESIREDIV"]       = VARS::post("DESIREDIV-".$receptno);
            $this->arrInput[$receptno]["JIZEN_SENGAN"]    = VARS::post("JIZEN_SENGAN-".$receptno);
            $this->arrInput[$receptno]["CLUB_CD"]         = VARS::post("CLUB_CD-".$receptno);
            $this->arrInput[$receptno]["HONOR_REMARK"]    = VARS::post("HONOR_REMARK-".$receptno);

            //事前特待、特待申請、資格活用を優先順位順に配列に格納
            $arrKakuteiTokutaiRank = array();
            $setKakuteiTokutaiRank = "";
            foreach ($arrTokutaiRank as $val) {
                if ($val["HONORDIV"] == $this->arrInput[$receptno]["JIZEN_TOKUTAI"]
                    || $val["HONORDIV"] == $this->arrInput[$receptno]["TOKUTAI_SINSEI"]
                    || $val["HONORDIV"] == $this->arrInput[$receptno]["SIKAKU_KATSUYO"]) {
                    $arrKakuteiTokutaiRank[] = $val["HONORDIV"];
                }
            }

            if (!empty($arrKakuteiTokutaiRank)) {
                $setKakuteiTokutaiRank = $arrKakuteiTokutaiRank[0];
            }

            //特待の確定ボタン押下時
            if ($this->cmd == "updateTokutaiKakutei") {
                //事前特待、特待申請、資格活用のうち最小ランク値（最大優先）の特待を判断し、確定特待に設定
                $this->arrInput[$receptno]["KAKUTEI_TOKUTAI"] = $setKakuteiTokutaiRank;
            }
        }

        //更新
        knjl071aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //CSV出力
    public function getDownloadCsvModel()
    {
        $db = Query::dbCheckOut();

        //ヘッダー
        $header = array();
        $header["RECEPTNO"]             = "受験番号";
        $header["NAME"]                 = "氏名";
        $header["NAME_KANA"]            = "氏名かな";
        $header["SEX_ABBV"]             = "性別";
        $header["FS_CD"]                = "出身学校コード";
        $header["FINSCHOOL_NAME_ABBV"]  = "出身学校";
        $header["SHDIV_NAME"]           = "専併区分";
        $header["COURSE_NAME"]          = "志望コース";
        if ($this->applicantdiv == "1") {
            $header["TOTAL"]                = "判定合計";
        } else {
            $header["TOTAL"]                = "５科合計";
        }
        $header["KAKUTEI_TOKUTAI_NAME"] = "確定特待";
        $header["JIZEN_TOKUTAI_NAME"]   = "事前特待";
        $header["TOKUTAI_SINSEI_NAME"]  = "特待申請";
        $header["SIKAKU_KATSUYO_NAME"]  = "資格活用";
        $header["DESIREDIV_NAME"]       = "事前判定";
        $header["JIZEN_SENGAN_NAME"]    = "事前専願";
        $header["CLUB_NAME"]            = "クラブ名";
        $header["HONOR_REMARK"]         = "特待備考";

        //初期化
        $contents = ""; //クリア
        $data = false; //出力フラグ

        //ヘッダ
        $contents .= implode(",", $header) . "\n";

        //データ
        $query = knjl071aQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"), "↓", str_replace(",", ".", $row));

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep . $row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        //受験校種
        $appdivRow = $db->getRow(knjl071aQuery::getNameMst($this->ObjYear, "L003", $this->applicantdiv), DB_FETCHMODE_ASSOC);
        $appdivName = $appdivRow["NAME1"];
        //試験回
        $testdivRow = $db->getRow(knjl071aQuery::getTestdivMst($this, $this->testdiv), DB_FETCHMODE_ASSOC);
        $testdivName = $testdivRow["TESTDIV_NAME"];

        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $fname  = $this->ObjYear."年度";
            $fname .= "_特待生CSV";
            $fname .= "_".$appdivName;
            $fname .= "_".$testdivName;
            $filename = sprintf("%s.csv", $fname);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
