<?php

require_once('for_php7.php');

class knjf072Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    function knjf072Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init() {
        $this->field = array(
                             "SEX"    =>  VARS::post("SEX"),     //性別
                             "DBNAME" =>  VARS::post("DBNAME")   //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }

    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        $comma = ",";
        $lf = "\n";

        //DB接続
        $db = Query::dbCheckOut();
        $schoolName = $db->getOne(knjf072Query::getSchoolName());
        Query::dbCheckIn($db);

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
        $title = "学校保健（健康状態）統計調査集計表（高校）,";
        $wrk_date = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),0);

        $contents = $wrk_nendo ."（".CTRL_YEAR."年度）," .$title .",,,,," ."作成日：" .$wrk_date .$lf;
        $contents .= $lf;

        $setTexst = (2018 > CTRL_YEAR) ? "いずれかが": "";
        $columns = array(
              "B" =>    "1,在籍数,,", 
              "C1" =>   "2,裸眼視力,視力の総受検者数,", 
              "C2" =>   ",,(１)裸眼視力測定者数,", 
              "C2_A" => ",,(２)１．０以上　　　　　　　・・・Ａ,", 
              "C2_B" => ",,(３)１．０未満　０．７以上・・・Ｂ,", 
              "C2_C" => ",,(４)０．７未満　０．３以上・・・Ｃ,", 
              "C2_D" => ",,(５)０．３未満　　　　　　　・・・Ｄ,", 
              "C3" =>   ",,(６)裸眼視力測定省略者,", 
              "D1" =>   "3,眼,受検者数,", 
              "D2" =>   ",,(１)アレルギー性眼疾患,", 
              "D3" =>   ",,(２)その他の眼疾病・異常,", 
              "E1" =>   "4,聴力,受検者数,", 
              "E2" =>   ",,難聴(聞き取りできなかった者),", 
              "F1" =>   "5,耳鼻咽頭,受検者数,", 
              "F2" =>   ",,(１)耳疾患,", 
              "F3" =>   ",,(２)アレルギー性鼻疾患,", 
              "F4" =>   ",,(３)その他の鼻・副鼻腔疾患,", 
              "F5" =>   ",,(４)口腔咽喉頭の疾患・異常,", 
              "G1" =>   "6,歯・口腔,受検者数,", 
              "G2" =>   ",,(１)虫歯(う歯),ア処置完了者", 
              "G3" =>   ",,,イ未処置歯保有者", 
              "G4" =>   ",,(２)要観察歯保有者,", 
              "G5" =>   ",,(３)CO要相談（観察）,", 
              "G6" =>   ",,(４)顎間接の異常,", 
              "G7" =>   ",,(５)歯列・咬合の異常,", 
              "G8" =>   ",,(６)歯垢の状態,", 
              "G9" =>   ",,(７)歯肉の状態,", 
              "G10" =>  ",,(８)その他の歯・口腔の疾病・異常,", 
              "H" =>    "7,受検者数(内科),,", 
              "I1" =>   "8,栄養状態,(１)肥満傾向　肥満度が２０％以上の者,", 
              "I2" =>   ",,(２)やせ傾向　肥満度が－２０％以下の者,", 
              "I3" =>   ",,(３)学校医の総合判断,", 
              "J1" =>   "9,脊柱・胸郭・四肢,脊柱・胸郭の疾病及び異常の有無,", 
              "J2" =>   ",,四肢の異常の有無,", 
              "K1" =>   "10,皮膚,(１)アトピー性皮膚炎,", 
              "K2" =>   ",,(２)その他の皮膚疾患,", 
              "L1" =>   "11,結核,受検者数,", 
              "L2" =>   ",,(１)結核患者,", 
              "L3" =>   ",,(２)精密検査対象者,", 
              "M1" =>   "12,心臓,受検者数(心電図検査),", 
              "M2" =>   ",,(１)心臓の疾患・異常,", 
              "M3" =>   ",,(２)心電図異常,", 
              "N1" =>   "13,尿,受検者数,", 
              "N2" =>   ",,(１)尿蛋白検出者,", 
              "N3" =>   ",,(２)尿糖検出者,", 
              "N4" =>   ",,(３)尿潜血検出者,", 
              "O1" =>   "14,その他,(１)ぜん息,", 
              "O2" =>   ",,(２)腎臓疾患,", 
              "O3" =>   ",,(３)言語障害,", 
              "O4" =>   ",,(４)その他の疾病・異常,", 
            );
        $grades = array();
        $contentsa = array();
        //DB接続
        $db = Query::dbCheckOut();
        $sexname = "";
        $query = knjf072Query::getNameMst("Z002", $this->field["SEX"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $sexname = $row["NAME2"];
        }
        //出力データの作成//
        //SQL文発行
        $query = knjf072Query::selectCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $grades[] = ('99' == $row["GRADE"] ? "合計" : $row["GRADE"]);
            foreach ($columns as $column => $colname) {
                $contentsa[$column] .= ($row[$column] ? $row[$column] : "0") . $comma;
            }
            $data = true;
        }
        
        $contents .= "性別" . $comma . $sexname .$lf;
        $contents .= "" . $comma . $comma . $comma . $comma . "担当者名" .$lf;
        $contents .= "学校名" . $comma . $schoolName . $comma . $comma . $comma . "連絡先 TEL" .$lf;
        $contents .= "NO" . $comma . "項目" . $comma . $comma . "学年" . $comma . implode($grades, $comma) .$lf;
        foreach ($columns as $column => $colname) {
            $contents .= $colname . $comma . $contentsa[$column] . $lf;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse="学校保健（健康状態）統計調査集計表（高校）";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
