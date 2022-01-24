<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_l510iModel extends Model
{
    public $field      = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード

    /**
     * メンバ変数の初期化
     */
    public function knjx_l510iModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array("TESTDIV0"         =>  VARS::post("TESTDIV0"),          //学科
                             "TESTDIV"          =>  VARS::post("TESTDIV"),           //入試区分
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");
        $this->year = VARS::request("YEAR") ? VARS::request("YEAR") : "";
        $this->applicantdiv = VARS::request("APPLICANTDIV") ? VARS::request("APPLICANTDIV") : "";

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function &getMainModel()
    {
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //ヘッダデータ
        $header = array();
        $header["ROW_NUMBER"]               = "No.";
        $header["EXAMNO"]                   = "受験番号";
        $header["TESTDIV0"]                 = "受験学科";
        $header["TESTDIV"]                  = "試験区分";
        $header["HOPE_COURSE1"]             = "志望順位1コース";
        $header["HOPE_COURSE2"]             = "志望順位2コース";
        $header["HOPE_COURSE3"]             = "志望順位3コース";
        $header["HOPE_COURSE4"]             = "志望順位4コース";
        $header["SEQ031_REMARK6"]           = "奨学志望";
        $header["DORMITORY_FLG"]            = "入寮志望";
        $header["NAME_KANA_KATAKANA"]       = "氏名ｶﾅ＋受験番号";
        $header["NAME_KANA_HIRAGANA"]       = "氏名ふりがな＋受験番号";
        $header["NAME_KANJI"]               = "氏名漢字＋受験番号";
        $header["BIRTHDAY"]                 = "生年月日";
        $header["SEX"]                      = "性別";
        $header["FINSCHOOL_NAME"]           = "中学校名";
        $header["FINSCHOOLCD"]              = "中学校コード";
        $header["FS_GRDYEAR"]               = "卒業年度";
        $header["GKANA_KATAKANA"]           = "保護者名ｶﾅ";
        $header["GKANA_HIRAGANA"]           = "保護者名ふりがな";
        $header["GNAME"]                    = "保護者名漢字";
        $header["GZIPCD"]                   = "郵便番号";
        $header["GADDRESS"]                 = "住所";
        $header["GTELNO"]                   = "電話番号";

        $contents  = "";    //クリア
        $header_data = false;   //出力フラグ

        //データ
        if ($this->field["HEADER"]=="on") {
            $contents .= implode(",", $header)."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knjx_l510iQuery::selectMainQuery($this);

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                //奨学志望
                if ($field == "SEQ031_REMARK6") {
                    $row[$field] = $row[$field] == "1" ? "有" : "";
                }
                //氏名ｶﾅ＋受験番号
                if ($field == "NAME_KANA_KATAKANA") {
                    $row[$field] = mb_convert_kana($row["NAME_KANA"], "k").$row["EXAMNO"];
                }
                //氏名ふりがな＋受験番号
                if ($field == "NAME_KANA_HIRAGANA") {
                    $row[$field] = mb_convert_kana($row["NAME_KANA"], "HVc").$row["EXAMNO"];
                }
                //氏名漢字＋受験番号
                if ($field == "NAME_KANJI") {
                    $row[$field] = $row["NAME"].$row["EXAMNO"];
                }
                //生年月日
                if ($field == "BIRTHDAY") {
                    $row[$field] = str_replace("-", "", $row[$field]);
                }
                //保護者名ｶﾅ
                if ($field == "GKANA_KATAKANA") {
                    $row[$field] = mb_convert_kana($row["GKANA"], "k");
                }
                //保護者名ふりがな
                if ($field == "GKANA_HIRAGANA") {
                    $row[$field] = mb_convert_kana($row["GKANA"], "HVc");
                }

                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = $this->year."年度入試"."　志願者基礎データ出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
