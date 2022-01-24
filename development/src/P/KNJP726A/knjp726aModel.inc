<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjp726aModel extends Model
{
    public $cmd;
    public $control;
    public $file;
    public $field = array();
    public $data_arr = array();
    public $z010Name1;

    /**
     * メンバ変数の初期化
     */
    public function knjp726aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),    //校種
                             "GRADE"            =>  VARS::post("GRADE"),          //学年
                             "GRADE_HR"         =>  VARS::post("GRADE_HR"),       //年組
                             "HEADER"           =>  VARS::post("HEADER")          //ヘッダ有
                             );

        $this->getPropertiesAll();
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //CSV出力(データ)
    public function outputDataFile()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        if ($this->field['HEADER'] == 'on') {
            //ヘッダ
            $contents .= implode(",", $this->getHeadArray($db))."\n";
        }

        $header_data = false;
        //SQL文発行
        $query = knjp726aQuery::getCsvData($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            common::downloadContents($contents, "学費口座データ.csv");

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function getHeadArray($db)
    {
        $retHeadArray = array();
        $retHeadArray[] = "学校区分";
        $retHeadArray[] = "学籍番号";
        $retHeadArray[] = "学年";
        $retHeadArray[] = "クラス";
        $retHeadArray[] = "出席番号";
        $retHeadArray[] = "課程学科コード";
        $retHeadArray[] = "課程学科名称";
        $retHeadArray[] = "コースコード";
        $retHeadArray[] = "コース名";
        $retHeadArray[] = "性別コード";
        $retHeadArray[] = "性別名称";
        $retHeadArray[] = "生年月日";
        $retHeadArray[] = "氏名";
        $retHeadArray[] = "氏名カナ";
        $retHeadArray[] = "氏名英字";
        $retHeadArray[] = "郵便番号";
        $retHeadArray[] = "生徒住所";
        $retHeadArray[] = "生徒住所方書き";
        $retHeadArray[] = "本人・電話番号";
        $retHeadArray[] = "本人・メール";
        $retHeadArray[] = "備考１";
        $retHeadArray[] = "備考２";
        $retHeadArray[] = "備考３";
        $retHeadArray[] = "国籍１コード";
        $retHeadArray[] = "国籍１名称";
        $retHeadArray[] = "国籍２コード";
        $retHeadArray[] = "国籍２名称";
        $retHeadArray[] = "保護者氏名";
        $retHeadArray[] = "保護者氏名カナ";
        $retHeadArray[] = "保護者続柄コード";
        $retHeadArray[] = "保護者続柄名称";
        $retHeadArray[] = "送付先郵便番号";
        $retHeadArray[] = "送付先住所";
        $retHeadArray[] = "送付先住所方書き";
        $retHeadArray[] = "保護者・電話番号";
        $retHeadArray[] = "保護者・電話番号２";
        $retHeadArray[] = "保護者・メール";
        $retHeadArray[] = "連絡先名称１";
        $retHeadArray[] = "連絡先氏名１";
        $retHeadArray[] = "連絡先電話番号１＿１";
        $retHeadArray[] = "連絡先電話番号１＿２";
        $retHeadArray[] = "連絡先名称２";
        $retHeadArray[] = "連絡先氏名２";
        $retHeadArray[] = "連絡先電話番号２＿１";
        $retHeadArray[] = "連絡先電話番号２＿２";
        $retHeadArray[] = "保護者役員名など";
        $retHeadArray[] = "受験番号";
        $retHeadArray[] = "入学日";
        $retHeadArray[] = "卒業日";
        $retHeadArray[] = "卒業番号";
        $retHeadArray[] = "出身校番号";
        $retHeadArray[] = "出身校名";
        $retHeadArray[] = "出身校卒業日";
        $retHeadArray[] = "銀行名(漢字)１";
        $retHeadArray[] = "銀行名(カナ)１";
        $retHeadArray[] = "支店名(カナ)１";
        $retHeadArray[] = "支店番号１";
        $retHeadArray[] = "預金種目１";
        $retHeadArray[] = "口座番号１";
        $retHeadArray[] = "預金者１";
        $retHeadArray[] = "銀行名(漢字)２";
        $retHeadArray[] = "銀行名(カナ)２";
        $retHeadArray[] = "支店名(カナ)２";
        $retHeadArray[] = "支店番号２";
        $retHeadArray[] = "預金種目２";
        $retHeadArray[] = "口座番号２";
        $retHeadArray[] = "預金者２";
        $retHeadArray[] = "通学区間１";
        $retHeadArray[] = "通学区間２";
        $retHeadArray[] = "通学区間３";
        $retHeadArray[] = "通学区間４";
        $retHeadArray[] = "通学区間５";
        $retHeadArray[] = "通学区間６";
        $retHeadArray[] = "通学区間７";
        $retHeadArray[] = "通学区間８";
        return $retHeadArray;
    }
}
