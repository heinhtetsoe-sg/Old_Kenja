<?php
/**
 * 定数定義
*/

class knjl633fModel extends Model
{
    public $field      = array();
    public $cmd;
    public $ObjYear;
    public $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    public function knjl633fModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR + 1;
    }

    public function init()
    {
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "EXCEPT_KESSEKI" => VARS::post("EXCEPT_KESSEKI") //欠席者は除く
                     );

        $this->cmd   = VARS::request("cmd");
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //入試制度名称
        $applicantdivName = $db->getOne(knjl633fQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));

        //入試区分名称
        $namecd1 = ($this->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $testdivName = $db->getOne(knjl633fQuery::getNameMst($this->ObjYear, $namecd1, $this->field["TESTDIV"]));

        //高校 内申評定
        $query = knjl633fQuery::getNameMst($this->ObjYear, "L008");
        $result = $db->query($query);
        $kyouka_count = 0;
        $kyouka_array = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $kyouka_array[$row["VALUE"]] = $row["ABBV1"];
            $kyouka_count++;
        }
        $result->free();

        //受験科目
        $query = knjl633fQuery::getNameMst($this->ObjYear, "L009");
        $result = $db->query($query);
        $nyushikamoku_array = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAME".$this->field["APPLICANTDIV"]] == "") {
                continue;
            }
            $nyushikamoku_array[$row["VALUE"]] = $row["NAME".$this->field["APPLICANTDIV"]];
        }
        $result->free();

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/

        $nendo = $this->ObjYear."/04/01";
        $nendo = common::DateConv1($nendo, 10);  //年度

        $title = $nendo.' '.$applicantdivName.' '.$testdivName.' 全項目';

        //項目ヘッダ
        $header = array();
        $header["APPLICANTDIV"]           = "入試制度コード";
        $header["APPLICANTDIV_NAME"]      = "入試制度名称";
        $header["TESTDIV"]                = "入試区分コード";
        $header["TESTDIV_NAME"]           = "入試区分名称";
        $header["SHDIV"]                  = "専併区分コード";
        $header["SHDIV_NAME"]             = "専併区分名称";
        $header["EXAMCOURSE"]             = "第一志望コード";
        $header["EXAMCOURSE_NAME"]        = "第一志望名称";
        $header["EXAMCOURSE2"]            = "第二志望コード";
        $header["EXAMCOURSE2_NAME"]       = "第二志望名称";
        $header["EIKEN_CD"]               = "英語見なしコード";
        $header["EIKEN_NAME"]             = "英語見なし名称";
        $header["TESTDIV1"]               = "受験科目数コード";
        $header["TESTDIV1_NAME"]          = "受験科目数名称";
        $header["EXAMNO"]                 = "受験番号";
        $header["NAME"]                   = "受験者氏名";
        $header["NAME_KANA"]              = "氏名かな";
        $header["BIRTHDAY"]               = "生年月日";
        $header["FS_CD"]                  = "出身学校コード";
        $header["FINSCHOOL_NAME"]         = "出身学校名";
        $header["ZIPCD"]                  = "郵便番号";
        $header["ADDRESS1"]               = "住所1";
        $header["ADDRESS2"]               = "住所2";
        $header["TELNO"]                  = "電話番号";
        $header["GNAME"]                  = "保護者氏名";
        $header["GKANA"]                  = "保護者かな";
        $header["RELATIONSHIP"]           = "保護者続柄コード";
        $header["RELATIONSHIP_NAME"]      = "保護者続柄名称";
        if ($this->field["APPLICANTDIV"] == "2") {
            $header["TOKUBETU_JYOKEN"]        = "特別条件";
        }
        $header["SHOUGAKU_CD"]            = "奨学区分コード";
        $header["SHOUGAKU_NAME"]          = "奨学区分名称";

        if ($this->field["APPLICANTDIV"] == "1") {
            $header["TOKU_KATU"]              = "特別活動";
            $header["BIKOU"]                  = "備考";
        } else {
            $header["JIZENSODAN_TEXT"]        = "事前相談 文言";
            $header["JIZENSODAN_CD"]          = "事前相談 コード";
            $header["JIZENSODAN_NAME"]        = "事前相談 名称";
        }

        if ($this->field["APPLICANTDIV"] == "1") {
            //第1回プレテスト
            $header["PRE_KOKUGO1"]            = "第1回プレテスト 国語得点";
            $header["PRE_SANSUU1"]            = "第1回プレテスト 算数得点";
            $header["PRE_EIGO1"]              = "第1回プレテスト 英語得点";
            $header["PRE_2KA1"]               = "第1回プレテスト 2教科合計";
            $header["PRE_3KA1"]               = "第1回プレテスト 3教科200点換算";
            $header["PRE_TOTAL1"]             = "第1回プレテスト 合計";
            //第2回プレテスト
            $header["PRE_KOKUGO2"]            = "第2回プレテスト 国語得点";
            $header["PRE_SANSUU2"]            = "第2回プレテスト 算数得点";
            $header["PRE_EIGO2"]              = "第2回プレテスト 英語得点";
            $header["PRE_2KA2"]               = "第2回プレテスト 2教科合計";
            $header["PRE_3KA2"]               = "第2回プレテスト 3教科200点換算";
            $header["PRE_TOTAL2"]             = "第2回プレテスト 合計";
        } else {
            //内申評定
            foreach ($kyouka_array as $key => $value) {
                $header["CONFIDENTIAL_RPT{$key}"]     = "内申評定 {$value}";
            }
            $header["JITURYOKU_3GOKEI"]       = "実力テスト 3科合計";
            $header["JITURYOKU_3HEIKIN"]      = "実力テスト 3科平均";
            $header["JITURYOKU_3TOKUTENRITU"] = "実力テスト 3科得点率";
            $header["JITURYOKU_5GOKEI"]       = "実力テスト 5科合計";
            $header["JITURYOKU_5HEIKIN"]      = "実力テスト 5科平均";
            $header["JITURYOKU_5TOKUTENRITU"] = "実力テスト 5科得点率";
            $header["CLUB_NAME"]              = "クラブ名";
            $header["TOKKI"]                  = "特活・部活・特記事項";
        }

        $header["KESSEKI"]                = "欠席";

        foreach ($nyushikamoku_array as $key => $value) {
            $header["NYUSHI_SCORE{$key}"]     = "入試情報 {$value}得点";
        }
        $header["EIGO_MINASHI"]           = "入試情報 英語見なし得点";

        if ($this->field["APPLICANTDIV"] == "1") {
            $header["TOTAL2"]                 = "入試情報 2教科合計";
            $header["TOTAL4"]                 = "入試情報 3教科200点換算";
            $header["TOTAL3"]                 = "入試情報 合計";
        } else {
            $header["EIGO_SCORE"]             = "入試情報 英語高いほう";
            $header["TOTAL3"]                 = "入試情報 3科合計";
            $header["TOTAL4"]                 = "入試情報 5科合計";
            $header["NYUSHI_3DEVIATION"]      = "入試情報 3科偏差値";
            $header["NYUSHI_5DEVIATION"]      = "入試情報 5科偏差値";
        }
        $header["JUDGEMENT"]              = "合否情報 合否コード";
        $header["JUDGEMENT_NAME"]         = "合否情報 合否名称";
        $header["COURSEMAJOR"]            = "合否情報 合格コースコード";
        $header["COURSEMAJOR_NAME"]       = "合否情報 合格コース名称";
        $header["PROCEDUREDIV"]           = "手続情報 区分コード";
        $header["PROCEDUREDIV_NAME"]      = "手続情報 区分名称";
        $header["ENTDIV"]                 = "入学情報 区分コード";
        $header["ENTDIV_NAME"]            = "入学情報 区分名称";

        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        //ヘッダ
        $contents .= implode(",", $header)."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //データ
        $query = knjl633fQuery::selectDataQuery($this, $kyouka_array, $nyushikamoku_array);

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"), "", str_replace(",", ".", $row));
            $setArray = $header;

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($setArray as $field => $label) {
                if ($field == "BIRTHDAY") {
                    $val = str_replace("-", "/", $row[$field]);
                    $contents .= $sep.$val;
                } elseif ($field == "APPLICANTDIV_NAME") {
                    $contents .= $sep.$applicantdivName;
                } elseif ($field == "TESTDIV_NAME") {
                    $contents .= $sep.$testdivName;
                } elseif ($field == "JITURYOKU_3TOKUTENRITU") {
                    $setTokutenRitsu = "";
                    if ($row["JITURYOKU_3GOKEI"] > 0 && $row["JITURYOKU_3HEIKIN"] > 0) {
                        $setTokutenRitsu = $row["JITURYOKU_3GOKEI"] / $row["JITURYOKU_3HEIKIN"] * 100;
                        $setTokutenRitsu = round($setTokutenRitsu, 1);
                    }
                    $contents .= $sep.$setTokutenRitsu;
                } elseif ($field == "JITURYOKU_5TOKUTENRITU") {
                    $setTokutenRitsu = "";
                    if ($row["JITURYOKU_5GOKEI"] > 0 && $row["JITURYOKU_5HEIKIN"] > 0) {
                        $setTokutenRitsu = $row["JITURYOKU_5GOKEI"] / $row["JITURYOKU_5HEIKIN"] * 100;
                        $setTokutenRitsu = round($setTokutenRitsu, 1);
                    }
                    $contents .= $sep.$setTokutenRitsu;
                } else {
                    $contents .= $sep.$row[$field];
                }
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

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
}
?>
