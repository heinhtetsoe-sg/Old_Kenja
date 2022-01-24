<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjd321cModel extends Model
{
    public $field = array();
    public $cmd;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjd321cModel()
    {
    }

    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),     //出力取込種別
                             "CHK_OVER"         =>  VARS::post("CHK_OVER")  //欠課時数オーバー
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    //データ出力
    public function getDataModel()
    {
        $header = array("SCHREGNO"                  => "学籍番号",
                        "NAME"                      => "生徒氏名",
                        "YEAR"                      => "年度",
                        "GRADE"                     => "学年",
                        "CLASSCD"                   => "教科コード",
                        "SCHOOL_KIND"               => "校種",
                        "CURRICULUM_CD"             => "教育課程コード",
                        "SUBCLASSCD"                => "科目コード",
                        "SUBCLASSNAME"              => "科目名",
                        "JUDGE_PATTERN"             => "パーターン",
                        "PATTERN_ASSESS"            => "評定",
                        "CREDITS"                   => "単位",
                        "KEKKA_JOUGEN"              => "欠課上限",
                        "KEKKA_JISUU"               => "欠課時数",
                        "KEKKA_OVER"                => "欠課時数オーバー",
                        $this->lastColumn           => $this->lastColumn);

        $contents  = "";
        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjd321cQuery::getMainSQL($this);
        $result = $db->query($query);
        $header_data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);


        if ($header_data) {
            $couse = "成績不振者";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 項目チェック
     */
    public function koumokuCheck($data)
    {
        $koumoku_error = "";        //項目エラー用
        $examLen = $this->Properties["examnoLen"] ? $this->Properties["examnoLen"] : "5";

        //桁数チェック NO001
        if (strlen($data["SCHREGNO"]) > 8 ||
            strlen($data["INOUTCD"]) > 1 ||
            strlen($data["NAME"]) > 120 ||
            strlen($data["NAME_SHOW"]) > 30 ||
            strlen($data["NAME_KANA"]) > 240 ||
            strlen($data["NAME_ENG"]) > 40 ||
            strlen($data["REAL_NAME"]) > 120 ||
            strlen($data["REAL_NAME_KANA"]) > 240 ||
            strlen($data["BIRTHDAY"]) > 10 ||
            strlen($data["SEX"]) > 1 ||
            strlen($data["BLOODTYPE"]) > 2 ||
            strlen($data["BLOOD_RH"]) > 1 ||
            strlen($data["HANDICAP"]) > 3 ||
            strlen($data["NATIONALITY"]) > 3 ||
            strlen($data["SCHOOL_KIND"]) > 2 ||
            strlen($data["FINSCHOOLCD"]) > 7 ||
            strlen($data["FINISH_DATE"]) > 10 ||
            strlen($data["PRISCHOOLCD"]) > 7 ||
            strlen($data["CURRICULUM_YEAR"]) > 4 ||
            strlen($data["ENT_DATE"]) > 10 ||
            strlen($data["ENT_DIV"]) > 1 ||
            strlen($data["ENT_REASON"]) > 75 ||
            strlen($data["ENT_SCHOOL"]) > 75 ||
            strlen($data["ENT_ADDR"]) > 90 ||
            ($this->Properties["useAddrField2"] == "1" && strlen($data["ENT_ADDR2"]) > 90) ||
            strlen($data["GRD_DATE"]) > 10 ||
            strlen($data["GRD_DIV"]) > 1 ||
            strlen($data["GRD_REASON"]) > 75 ||
            strlen($data["GRD_SCHOOL"]) > 75 ||
            strlen($data["GRD_ADDR"]) > 90 ||
            ($this->Properties["useAddrField2"] == "1" && strlen($data["GRD_ADDR2"]) > 90) ||
            strlen($data["GRD_NO"]) > 8 ||
            strlen($data["GRD_TERM"]) > 4 ||
            strlen($data["REMARK1"]) > 75 ||
            strlen($data["REMARK2"]) > 75 ||
            strlen($data["REMARK3"]) > 75 ||
            strlen($data["EMERGENCYCALL"]) > 60 ||
            strlen($data["EMERGENCYNAME"]) > 60 ||
            strlen($data["EMERGENCYRELA_NAME"]) > 30 ||
            strlen($data["EMERGENCYTELNO"]) > 14 ||
            strlen($data["EMERGENCYCALL2"]) > 60 ||
            strlen($data["EMERGENCYNAME2"]) > 60 ||
            strlen($data["EMERGENCYRELA_NAME2"]) > 30 ||
            strlen($data["EMERGENCYTELNO2"]) > 14 ||
            strlen($data["EXAMNO"]) > $examLen) {
            $koumoku_error .= "桁数エラー。";
        }
        //必須チェック
        if ($data["SCHREGNO"]=="" || $data["NAME"]=="" || $data["SCHOOL_KIND"]=="") {
            $koumoku_error .= "必須項目エラー。";
        }
        //数字チェック
        if (!$this->suujiCheck($data["INOUTCD"])       ||
            !$this->suujiCheck($data["SEX"])           ||
            !$this->suujiCheck($data["HANDICAP"])      ||
            !$this->suujiCheck($data["NATIONALITY"])   ||
            !$this->suujiCheck($data["FINSCHOOLCD"])   ||
            !$this->suujiCheck($data["PRISCHOOLCD"])   ||
            !$this->suujiCheck($data["ENT_DIV"])       ||
            !$this->suujiCheck($data["GRD_DIV"])) {
            $koumoku_error .= "数字項目エラー。";
        }
        //値チェック
        if (!$this->ataiCheck($data["SEX"], array("1","2"))            ||
//---2005.05.09
//          !$this->ataiCheck($data["ENT_DIV"]     ,array("1","2","3","4"))    ||
//          !$this->ataiCheck($data["GRD_DIV"]     ,array("1","2","3"))        ||
            !$this->ataiCheck($data["SCHOOL_KIND"], array("J","H","P"))        ||
            !$this->ataiCheck($data["BLOODTYPE"], array("A","B","O","AB"))   ||
            !$this->ataiCheck($data["BLOOD_RH"], array("-","+"))) {
            $koumoku_error .= "値エラー。";
        }

        return $koumoku_error;  //項目エラー用
    }

    //数字チェック
    public function suujiCheck($data)
    {
        $flg = true;

        if ($data!="") {
            if (!is_numeric($data)) {
                $flg = false;
            }
        }

        return $flg;
    }

    //値チェック
    public function ataiCheck($data, $atai)
    {
        $flg = true;

        if ($data!="") {
            if (!in_array($data, $atai)) {
                $flg = false;
            }
        }

        return $flg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data, $ketasuu)
    {
        $shosiki = "%0".$ketasuu."d";
        if ($data!="") {
            if (strlen($data) < $ketasuu) {
                $data = sprintf($shosiki, $data);
            }
        }

        return $data;
    }

    /**
     * マスタの存在チェック
     */
    public function sonzaiCheck(&$db, $data, $table, $where)
    {
        $sonzai_flg = true;
        if ($data!="") {
            $sonzai_flg = false;
            $query = knjd321cQuery::getMasterSQL($table, $where);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sonzai_flg = true;
            }
        }
        return $sonzai_flg;
    }
}
