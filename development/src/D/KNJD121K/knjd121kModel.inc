<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd121kModel extends Model {

    var $field      = array();
    var $flg        = array();
    var $data       = array();
    var $flg_data   = array();
    var $step       = array();
    var $control_cd = array();
    var $cmd;
    var $term;
    var $electdiv;
    /**
     * メンバ変数の初期化
     */
    function knjd121kModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        //プロパティファイル
        $this->getPropertiesAll();
        
        //選択
        $this->electdiv = VARS::post("ELECTDIV");

        //実行権限
        $this->sec_competence = AUTHORITY;

        //科目コード
        if(VARS::post("SUBCLASSCD")){
            $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");
        }else{
            $db = Query::dbCheckOut();
            $query = knjd121kQuery::selectSubclassQuery($this);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            $this->field["SUBCLASSCD"] = $row["SUBCLASSCD"];
            Query::dbCheckIn($db);
        }
        
        $this->cmd     = VARS::request("cmd");
        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }
        //講座コード
        if($this->cmd!="subclasscd"&&VARS::post("CHAIRCD")){
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else if ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        }else{
            $this->field["CHAIRCD"] = "";
        }

        $this->control_cd=array();
        //管理者コントロール
        $db = Query::dbCheckOut();
        $query = knjd121kQuery::selectContolCodeQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->control_cd[]=$row["CONTROL_CODE"];
        }
        Query::dbCheckIn($db);

        //学期成績データ項目名を配列にする。
        $term=array("SEM1_INTER_REC","SEM1_TERM_REC","SEM1_REC","SEM2_INTER_REC","SEM2_TERM_REC","SEM2_REC","SEM3_TERM_REC","SEM3_REC","GRADE_RECORD","PROV_FLG","GRADE_ASSESS");
        //学期成績データフラグ項目名を配列にする。
        $flg=array("SEM1_INTER_REC_FLG","SEM1_TERM_REC_FLG","SEM1_REC_FLG","SEM2_INTER_REC_FLG","SEM2_TERM_REC_FLG","SEM2_REC_FLG","SEM3_TERM_REC_FLG","SEM3_REC_FLG");

        //学期成績
        $this->fields=array();
        $this->flg_data_ar=array();
        for($j=0;$j<get_count($term);$j++){
            for($i=0;$i<get_count($this->data["SCHREGNO"]);$i++){
                $term_name=$term[$j]."-".$i;
                $flg_name=$flg[$j]."-".$i;
                $post_term = VARS::post($term_name);
                if(isset($post_term)){

                    //出欠情報が入ってたら成績は空
                    if (VARS::post($term_name)=="KK" | VARS::post($term_name)=="KS") {
                        $this->fields[$term[$j]."_DI"][] = $post_term;
                        $this->fields[$term[$j]][] = "";
                    } else {
                        $this->fields[$term[$j]."_DI"][] = "";
                        $this->fields[$term[$j]][] = $post_term;
                    }

#                    $this->flg_data_ar[$flg[$j]][] = (VARS::post($term_name)=="") ? Null : '0';
                    $this->flg_data_ar[$flg[$j]][] = (VARS::post($term_name)=="" | VARS::post($term_name)=="KK" | VARS::post($term_name)=="KS") ? Null : '0';
                } else {
                    $this->fields[$term[$j]][] = $this->data[$term_name];
                    $this->flg_data_ar[$flg[$j]][] = $this->flg_data[$flg_name];
                    $this->fields[$term[$j]."_DI"][] = $this->attend_data[$term_name];
                }
            }
        }
    
        //相対評価
        $this->GRADE3_RELAASSESS_5STEP = array();
        $this->GRADE3_RELAASSESS_10STEP = array();
        for($i=0;$i<get_count($this->data["SCHREGNO"]);$i++){
            
            //編集可能な場合はフォームの値を参照する。
            $post_step5 = VARS::post("GRADE3_RELAASSESS_5STEP"."-".$i);
            if (isset($post_step5)) {
                $this->GRADE3_RELAASSESS_5STEP[] = VARS::post("GRADE3_RELAASSESS_5STEP"."-".$i);      
            } else {
                $this->GRADE3_RELAASSESS_5STEP[] = $this->step["GRADE3_RELAASSESS_5STEP"."-".$i];
            }

            $post_step10 = VARS::post("GRADE3_RELAASSESS_10STEP"."-".$i);
            if (isset($post_step10)) {
                $this->GRADE3_RELAASSESS_10STEP[] = VARS::post("GRADE3_RELAASSESS_10STEP"."-".$i);      
            } else {
                $this->GRADE3_RELAASSESS_10STEP[] = $this->step["GRADE3_RELAASSESS_10STEP"."-".$i];
            }
        }

        ## 2005/11/09 ameku
        
        for($i=1;$i<=5;$i++)
        {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");

        ##ameku

        $this->cmd     = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getMainModel()
    {
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        /*  更新処理 */
        knjd121kQuery::update($this);
        $this->setMessage("MSG201");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
//var_dump($_REQUEST);
?>
