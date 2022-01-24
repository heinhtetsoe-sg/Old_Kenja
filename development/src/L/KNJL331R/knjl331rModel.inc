<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl331rModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $fixApplicantDiv;
        
    /**
     * メンバ変数の初期化
     */
    function knjl331rModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "PRINT_DIV"    =>  VARS::post("PRINT_DIV")         //出力範囲
                            );    

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd = VARS::request("cmd");
    }

    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        list($loginYear, $loginMon, $loginDay) = explode("-", str_replace("/", "-", CTRL_DATE));
        $loginDate  = $loginYear."年".sprintf("%d", $loginMon)."月".sprintf("%d", $loginDay)."日";
        $schoolName = $db->getOne(knjl331rQuery::getSchoolName($this));
        $printName = ($this->field["PRINT_DIV"] == "1") ? "合格者" : "入学者";
        $tilteName  = $this->ObjYear."年度　入学試験　".$printName."名簿";
        $header1 = array("NUMBER"            => $tilteName,
                         "EXAMNO"            => "",
                         "NAME"              => "",
                         "NAME_KANA"         => "",
                         "SEX"               => "",
                         "FINSCHOOL_NAME"    => $schoolName."　　　".$loginDate,
                         "GNAME"             => "",
                         "ZIPCD"             => "",
                         "ADDRESS"           => "",
                         "TELNO"             => "",
                         "REMARK"            => "");
        //ヘッダ
        $header = array("NUMBER"            => "No.",
                        "EXAMNO"            => "受験番号",
                        "NAME"              => "生徒氏名",
                        "NAME_KANA"         => "カナ氏名",
                        "SEX"               => "性別",
                        "FINSCHOOL_NAME"    => "出身校",
                        "GNAME"             => "保護者名",
                        "ZIPCD"             => "郵便番号",
                        "ADDRESS"           => "現住所",
                        "TELNO"             => "電話番号",
                        "REMARK"            => "備考");

        $contents  = "";
        $contents .= implode($header1, ",") ."\n";
        $contents .= implode($header, ",") ."\n";

        //CSVデータ取得
        $number  = 0;
        $dataFlg = false;
        $query  = knjl331rQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\"", "", $row);//データ内にダブルクォーテーションがあればブランクに置換
            $row["NUMBER"]  = ++$number;
            $row["REMARK"]  = "";

            foreach ($header as $key => $val) {
                $contents .= $row[$key] .",";
            }
            $contents .= "\n";

            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataFlg) {
            //ファイル名
            $schoolName = ($this->field["APPLICANTDIV"] == "1") ? "中学" : "高校";
            $printName = ($this->field["PRINT_DIV"] == "1") ? "合格者" : "入学者";
            $filename = $this->ObjYear."年度".$schoolName."入学試験".$printName."名簿.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
