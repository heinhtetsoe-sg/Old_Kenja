<?php

require_once('for_php7.php');

//定数定義
class knjf321Model extends Model {

    var $field = array();
    var $cmd;
    var $year = "";
    var $work_date = "";
    var $staffcd = "";

    //メンバ変数の初期化
    function knjf321Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->work_date);
            unset($this->staffcd);
            $this->field = array();
        }

        $this->field = array("WORK_DATE"      =>  VARS::post("WORK_DATE"),  //執務日時
                             "AM_PM_FLG"      =>  VARS::post("AM_PM_FLG"),  //執務時間帯
                             "STAFFCD"        =>  VARS::post("STAFFCD"),    //執務者
                             //時間帯
                             "REMARK1_901"    =>  VARS::post("REMARK1_901"),//午前
                             "REMARK2_901"    =>  VARS::post("REMARK2_901"),//午後
                             "REMARK3_901"    =>  VARS::post("REMARK3_901"),//全日
                             //執務場所
                             "REMARK1_000"    =>  VARS::post("REMARK1_000"),//学校
                             "REMARK2_000"    =>  VARS::post("REMARK2_000"),//校外施設チェック
                             "REMARK3_000"    =>  VARS::post("REMARK3_000"),//診療所等チェック
                             "REMARK4_000"    =>  VARS::post("REMARK4_000"),//研修会チェック
                             "REMARK5_000"    =>  VARS::post("REMARK5_000"),//講習会チェック
                             "REMARK6_000"    =>  VARS::post("REMARK6_000"),//その他チェック
                             "REMARK7_000"    =>  VARS::post("REMARK6_000"),//その他テキスト
                             //1.学校保健委員会等
                             "REMARK1_001"    =>  VARS::post("REMARK1_001"),//チェック
                             "REMARK2_001"    =>  VARS::post("REMARK2_001"),//(1)学校保健計画立案チェック
                             "REMARK3_001"    =>  VARS::post("REMARK3_001"),//(2)その他チェック
                             "REMARK4_001"    =>  VARS::post("REMARK4_001"),//その他テキスト
                             //2.学校環境衛生に関する指導助言
                             "REMARK1_002"    =>  VARS::post("REMARK1_002"),//チェック
                             //3.定期健康診断
                             "REMARK1_003"    =>  VARS::post("REMARK1_003"),//チェック
                             "REMARK2_003"    =>  VARS::post("REMARK2_003"),//(1)定期健康診断
                             "REMARK3_003"    =>  VARS::post("REMARK3_003"),//(2)結核検診
                             "REMARK4_003"    =>  VARS::post("REMARK4_003"),//(3)心臓検診
                             "REMARK5_003"    =>  VARS::post("REMARK5_003"),//(4)その他チェック
                             "REMARK6_003"    =>  VARS::post("REMARK6_003"),//その他テキスト
                             //4.臨時健康診断
                             "REMARK1_004"    =>  VARS::post("REMARK1_004"),//チェック
                             "REMARK2_004"    =>  VARS::post("REMARK2_004"),//(1)夏季施設等
                             "REMARK3_004"    =>  VARS::post("REMARK3_004"),//(2)修学旅行等
                             "REMARK4_004"    =>  VARS::post("REMARK4_004"),//(3)その他チェック
                             "REMARK5_004"    =>  VARS::post("REMARK5_004"),//その他テキスト
                             //5.伝染病及び食中毒発生時の指導助言及び予防処置
                             "REMARK1_005"    =>  VARS::post("REMARK1_005"),//チェック
                             //6.健康相談及び保健指導
                             "REMARK1_006"    =>  VARS::post("REMARK1_006"),//チェック
                             "REMARK2_006"    =>  VARS::post("REMARK2_006"),//(1)健康相談
                             "REMARK3_006"    =>  VARS::post("REMARK3_006"),//(2)保健指導
                             "REMARK4_006"    =>  VARS::post("REMARK4_006"),//(3)保険講話
                             "REMARK5_006"    =>  VARS::post("REMARK5_006"),//(4)その他チェック
                             "REMARK6_006"    =>  VARS::post("REMARK6_006"),//その他テキスト
                             //7.校長の求めによる救急処置
                             "REMARK1_007"    =>  VARS::post("REMARK1_007"),//チェック
                             //8.学校保健に関する研修会、講習会等
                             "REMARK1_008"    =>  VARS::post("REMARK1_008"),//チェック
                             //9.その他
                             "REMARK1_009"    =>  VARS::post("REMARK1_009"),//チェック
                             "REMARK2_009"    =>  VARS::post("REMARK2_009"),//その他テキスト
                             
                             "NEWS_STORY"     =>  VARS::post("NEWS_STORY"), //記事
                             "SPECIAL_REPORT" =>  VARS::post("SPECIAL_REPORT"),//特記事項
                             "INEI_PRI"       =>  VARS::post("INEI_PRI"),       //印影校長
                             "INEI_STF"       =>  VARS::post("INEI_STF"),       //印影執務者
                             "UPDATED"        =>  VARS::post("UPDATED")
                             );
        
        //左画面のコンボ、パラメータ用
        if (VARS::request("YEAR")){
            $this->year = VARS::request("YEAR");
        }
        if (VARS::get("WORK_DATE")){
            $this->work_date = VARS::get("WORK_DATE");
        }
        if (VARS::get("STAFFCD")){
            $this->staffcd = VARS::get("STAFFCD");
        }

        $this->cmd = VARS::request("cmd");
    }
    
    //入力項目チェック
    function validateInput()
    {
        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $this->field["WORK_DATE"]);
        $row = $db->getRow(knjf321Query::getRow($this->field["STAFFCD"], $date), DB_FETCHMODE_ASSOC);

        if ($this->cmd == "update" || $this->cmd == "delete"){
            //リストより選択していない
            if ($this->work_date == "" || $this->staffcd == "" ){
                $this->setWarning("MSG308");
                return false;
            }
            //キー変更をしての削除
            if($this->work_date != $this->field["WORK_DATE"] || $this->staffcd != $this->field["STAFFCD"]){
               $this->setWarning("MSG308");
               return false;
            }
            //存在チェック
            if (!is_array($row)){
                $this->setWarning("MSG303");
                $this->work_date = $this->field["WORK_DATE"];
                $this->staffcd = $this->field["STAFFCD"];
                return false;
            }
        }

        if ($this->cmd == "update" || $this->cmd == "add"){
            //必須項目チェック
            if($this->field["WORK_DATE"] == ""){
                $this->setWarning("MSG301","(執務日付)");
                return false;
            }
            if($this->field["STAFFCD"] == ""){
                $this->setWarning("MSG301","(執務者)");
                return false;
            }

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG302");
                $this->work_date = $this->field["WORK_DATE"];
                $this->staffcd   = $this->field["STAFFCD"];
                return false;
            }
            if (strlen($this->field["REMARK7_000"]) > 90 || strlen($this->field["REMARK4_001"]) > 90 || strlen($this->field["REMARK6_003"]) > 90 || strlen($this->field["REMARK5_004"]) > 90 
                || strlen($this->field["REMARK6_006"]) > 90 || strlen($this->field["REMARK2_009"]) > 90) {
                $this->setWarning("MSG915","(その他は90バイトまでです)");
                return false;
            }

            if ($this->validate_row_cnt($this->field["NEWS_STORY"], (20 * 2)) > 12) {
                $this->setWarning("MSG915","(記事は12行までです");
                return false;
            }
            
            if ($this->validate_row_cnt($this->field["SPECIAL_REPORT"], (20 * 2)) > 12) {
                $this->setWarning("MSG915","(特記事項は12行までです");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
        }

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    function &delete()
    {
        knjf321Query::getDeleteQuery($this);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
            $this->work_date = "";
            $this->staffcd = "";
        }
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert()
    {
        knjf321Query::getDeleteQuery($this);
        knjf321Query::getInsertQuery($this);
        if (!isset($model->warning)){
            $this->work_date = $this->field["WORK_DATE"];
            $this->staffcd   = $this->field["STAFFCD"];
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
