<?php
class knjl126iModel extends Model
{
    public $field  = array();
    public $cmd;
    public $examYear;

    public function init()
    {
        $this->examYear   = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "SEX"          =>  VARS::post("SEX"),              //抽出
                            );

        $this->cmd = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        //ヘッダー
        $header = array("ENTEXAMYEAR"        => "入試年度",
                        "TESTDIV"            => "入試区分",
                        "EXAMNO"             => "受験番号",
                        "NAME"               => "志願者氏名",
                        "NAME_KANA"          => "志願者氏名カナ",
                        "SEX"                => "性別",
                        "ATTEND_ALL_FLG"     => "出欠区分",
                        "JUDGE_KIND"         => "内定区分",
                        "JUDGEMENT"          => "合格区分",
                        "ENTDIV"             => "入学",
                        "BIRTHDAY"           => "生年月日",
                        "FS_CD"              => "出身校CD",
                        "FINSCHOOL_NAME"     => "出身校名",
                        "ZIPCD"              => "志願者郵便番号",
                        "ADDRESS1"           => "志願者住所１",
                        "ADDRESS2"           => "志願者住所２",
                        "TELNO"              => "志願者電話番号",
                        "GNAME"              => "保護者氏名",
                        "GKANA"              => "保護者氏名カナ",
                        "GZIPCD"             => "保護者郵便番号",
                        "GADDRESS1"          => "保護者住所１",
                        "GADDRESS2"          => "保護者住所２",
                        "GTELNO"             => "保護者電話番号",
                        "SEND_ZIPCD"         => "送付先郵便番号",
                        "SEND_ADDRESS1"      => "送付先住所１",
                        "SEND_ADDRESS2"      => "送付先住所２",
                        "SEND_TELNO"         => "送付先電話番号",
                        "SIBOU_RANK"         => "高等部志望順位",
                        "HEIGAN1"            => "併願校名１",
                        "HEIGAN2"            => "併願校名２",
                        "TOTAL_ALL"          => "９科評定",
                        "HYOTEI_1_2_TOTAL"   => "１・２年次評定合計",
                        "JITSUGI3_HYOTEI"    => "実技３科評定",
                        "ABSENCE_DAYS_TOTAL" => "３年間欠席日数",
                        "ABSENCE_DAYS3"      => "３年次欠席日数",
                        "TYOSASYO_PLUS"      => "+調査書",
                        "TYOSASYO_MINUS"     => "-調査書",
                        "DOUSOU"             => "同窓",
                        "EIKEN_KYU"          => "英検取得級",
                        "TYOUSEITEN_PLUS"    => "+調整点",
                        "TYOUSEITEN_MINUS"   => "-調整点",
                        "IN_A"               => "面接評価１",
                        "IN_B"               => "面接評価２",
                        "IN_C"               => "面接評価３",
                        "SCORE1"             => "面接ポイント",
                        "TOTAL_PLUS"         => "+合計",
                        "TOTAL_MINUS"        => "-合計",
                        "TESTSUBCLASSCD1"    => "国語",
                        "TESTSUBCLASSCD2"    => "数学",
                        "TESTSUBCLASSCD3"    => "英語",
                        "TOTAL1"             => "合計",
                        "SEX_RANK1"          => "性別順位",
                        );

        //クリア
        $contents  = "";
        //出力フラグ
        $data = false;

        //ヘッダ
        $contents .= implode(",", $header)."\n";

        $db = Query::dbCheckOut();
        $query = knjl126iQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));

            //値をセット
            $sep = "";
            foreach ($row as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        $query = knjl126iQuery::getApplicantDivName($this);
        $applicantDivName = $db->getOne($query);

        $query = knjl126iQuery::getEntexamTestDivMst($this, $this->field["TESTDIV"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $testdivAbbv = $row['TESTDIV_ABBV'];

        $sex = "";
        // 全員以外の場合
        if ($this->field["SEX"] != '3') {
            $query = knjl126iQuery::getSex($this);
            $sex = $db->getOne($query);
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $filename = sprintf("%s年度入試受験結果CSV出力（%s%s%s）.csv", $this->examYear, $applicantDivName, $testdivAbbv, $sex);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
