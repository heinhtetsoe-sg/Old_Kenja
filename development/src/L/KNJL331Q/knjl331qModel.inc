<?php

require_once('for_php7.php');
class knjl331qModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function knjl331qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "ABSENT"           =>  VARS::post("ABSENT")            //欠席者を除く
                         );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //科目取得
        $this->suisenTestSubCd = array();
        $query = knjl331qQuery::getTestSubclasscd($this, "3");//校長推薦
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->suisenTestSubCd[$row["VALUE"]] = $row["LABEL"];
        }
        $this->ippanTestSubCd = array();
        $query = knjl331qQuery::getTestSubclasscd($this, "5");//一般
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->ippanTestSubCd[$row["VALUE"]] = $row["LABEL"];
        }

        //ヘッダーセット
        $header = array();
        $header["EXAMNO"]                = "受験番号";
        $header["TESTDIV"]               = "入試区分";
        $header["EXAMCOURSE_MARK"]       = "受験コース記号";
        $header["NAME"]                  = "志願者氏名";
        $header["NAME_KANA"]             = "志願者氏名ふりがな";
        $header["SEX"]                   = "性別";
        $header["SYUKKETU_KUBUN"]        = "出欠区分";
        $header["JUDGEMENT"]             = "合否";
        $header["ENTDIV"]                = "入学";
        $header["BIRTHDAY"]              = "生年月日";
        $header["FS_CD"]                 = "出身学校コード";
        $header["FS_NAME"]               = "出身学校名";
        $header["FS_DAY"]                = "卒業年月";
        $header["ZIPCD"]                 = "志願者郵便番号";
        $header["ADDRESS1"]              = "志願者住所";
        $header["ADDRESS2"]              = "志願者方書き";
        $header["TELNO"]                 = "志願者電話番号";
        $header["GNAME"]                 = "保護者氏名";
        $header["GZIPCD"]                = "保護者郵便番号";
        $header["GADDRESS1"]             = "保護者住所";
        $header["GADDRESS2"]             = "保護者方書き";
        $header["GTELNO"]                = "保護者電話番号";
        $header["RELATIONSHIP"]          = "続柄";
        $header["SCHOLAR_KIBOU"]         = "スカラーシップ希望";
        $header["SCHOLAR_SAIYOU"]        = "スカラーシップ採用区分";
        $header["SCHOLAR_TOUKYU_SENGAN"] = "スカラーシップ等級";
        $header["DORMITORY_FLG"]         = "入寮希望";
        $header["SH_SCHOOLCD1"]          = "志望校第1志望";
        $header["SH_SCHOOLCD2"]          = "志望校第2志望";
        $header["SH_SCHOOLCD3"]          = "志望校第3志望";
        $header["SH_SCHOOLCD4"]          = "志望校第4志望";
        $header["KAKUYAKU_FLG"]          = "確約フラグ";
        $header["JUDGEMENT2"]            = "合否区分";
        $header["ENTDIV2"]               = "入学有無";
        $header["CONF1_AVG5"]            = "1年_5科平均評定";
        $header["CONF1_AVG9"]            = "1年_9科平均評定";
        $header["CONF2_AVG5"]            = "2年_5科平均評定";
        $header["CONF2_AVG9"]            = "2年_9科平均評定";
        $header["CONF3_AVG5"]            = "3年_5科平均評定";
        $header["CONF3_AVG9"]            = "3年_9科平均評定";
        $header["PUBLIC_SCORE1"]         = "公的試験1回目";
        $header["PUBLIC_SCORE2"]         = "公的試験2回目";
        $header["ABSENCE_DAYS"]          = "1年欠席日数";
        $header["ABSENCE_DAYS2"]         = "2年欠席日数";
        $header["ABSENCE_DAYS3"]         = "3年欠席日数";
        $header["ABSENCE_REMARK"]        = "1年欠席理由";
        $header["ABSENCE_REMARK2"]       = "2年欠席理由";
        $header["ABSENCE_REMARK3"]       = "3年欠席理由";
        $header["TOKKIJIKOU"]            = "調査書特記事項";
        $header["KATSUDOU_TOKKI"]        = "活動実績特記事項";
        foreach ($this->ippanTestSubCd as $key => $testName) {
            $header["I_".$key]                 = "一般".$testName;
        }
        $header["I_TOTAL"]               = "一般合計";
        $header["I_INTERVIEW_VALUE"]     = "一般面接";
        $header["SAT_NO"]                = "実戦模試受験番号";
        $header["SAT_TOTAL"]             = "実戦模試合計";
        $header["SAT_JUDGE"]             = "実戦模試判定";
        foreach ($this->suisenTestSubCd as $key => $testName) {
            $header["S_".$key]                 = "推薦".$testName;
        }
        $header["S_TOTAL"]               = "推薦合計";
        $header["S_INTERVIEW_VALUE"]     = "推薦面接";
        $header["S_COMPOSITION_VALUE"]   = "推薦作文";
        $header[$this->lastColumn]       = $this->lastColumn;

        $contents = ""; //CSVデータ出力用
        //ヘッダーセット
        $comma = "";
        foreach ($header as $key => $val) {
            $contents .= $comma.$header[$key];
            $comma = ",";
        }
        $contents .= "\n";

        $data = false;
        $query = knjl331qQuery::getCsvData($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $row = str_replace("\r\n", "", $row);

            foreach ($this->ippanTestSubCd as $key => $testName) {
                $row["I_TOTAL"] = $row["I_TOTAL"] + $row["I_".$key];//一般合計
            }
            foreach ($this->suisenTestSubCd as $key => $testName) {
                $row["S_TOTAL"] = $row["S_TOTAL"] + $row["S_".$key];//校長推薦合計
            }
            $comma = "";
            foreach ($header as $h_key => $val) {
                $contents .= $comma.$row[$h_key];
                $comma = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            $couse    = "作業用データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
