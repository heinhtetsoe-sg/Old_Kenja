<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja170oModel extends Model {
    var $field      = array();
    var $cmd;
    var $select_data    = array();
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knja170oModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),          //年度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),        //ラジオ選択データ
                             "TEL"              =>  VARS::post("TEL"),           //TEL
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS") //年組
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }

    function getDownloadModel()
    {
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        $contents = ""; //CSVデータ出力用
        $data = false;
        $tel_flg = $this->field["TEL"];
        $sday = str_replace("/","-",$this->control["学期開始日付"][$this->field["OUTPUT"]]);
        $eday = str_replace("/","-",$this->control["学期終了日付"][$this->field["OUTPUT"]]);
        $title = "生徒名簿";
//      $nendo = CTRL_YEAR."/04/01";
//      $nendo = common::DateConv1($nendo,10);                              //年度
//      $ctrl_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);   //作成日
//      $contents .= $nendo."　".$title."\r\n";
//      $contents .= "作成日 ： ".$ctrl_date."\r\n";
        //CLASS
        $result = $db->query(knja170oQuery::get_class_csv($this));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $result->free();
        //TEACHER
        $result = $db->query(knja170oQuery::get_teach_csv($this));
        $row2 = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $result->free();
        $nenkumi = $row["HR_NAME"];
        $tannin  = $row2["STAFFNAME_SHOW"];
        $contents .= "年組,担任名,生徒手帳番号,学級番号,氏名,ふりがな,生年月日,郵便番号,現住所,電話番号,保護者氏名,急用電話番号,中学校"."\r\n";
        //学籍番号ループ
        for($i=0;$i<get_count($this->select_data["selectdata"]);$i++){
            $schno = $this->select_data["selectdata"][$i];
            $result = $db->query(knja170oQuery::getcsvdata($this,$schno,$sday,$eday));
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $row = str_replace(",",".",$row);
            $contents .= $nenkumi.",";
            $contents .= $tannin.",";
            $contents .= $row["SCHREGNO"].",";
            $contents .= $row["ATTENDNO"].",";
            $contents .= $row["SEITO_KANJI"].",";
            $contents .= $row["SEITO_KANA"].",";
            $contents .= common::DateConv1(str_replace("-","/",$row["BIRTHDAY"]),0).",";
            $contents .= $row["ZIPCD1"].",";
            $contents .= $row["ADDRESS1"].",";
            //電話番号出力フラグ
            if ($tel_flg == 1) {
                $contents .= $row["TELNO1"].",";
                $contents .= $row["GUARD_NAME"].",";
                $contents .= $row["TELNO2"].",";
                $contents .= $row["J_NAME"].",";
            }else {
                $contents .= ",";
                $contents .= $row["GUARD_NAME"].",";
                $contents .= ",";
                $contents .= $row["J_NAME"].",";
            }
            $contents .= "\r\n";
            if($contents!="") $data = true;
            $result->free();
        }//学籍番号ループ

        //DB切断
        Query::dbCheckIn($db);

        if ($data){
            $couse="生徒名簿";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }//getDownloadModelの綴り

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
