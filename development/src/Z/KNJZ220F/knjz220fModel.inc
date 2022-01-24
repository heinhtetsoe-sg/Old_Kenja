<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}
class knjz220fModel extends Model {
        var $field       = array();
        var $low_stock   = array();
        var $high_stock  = array();
        var $cmd;
        var $level;
        var $staffcd;   //セキュリティー用スタッフコード
        var $sec_competence;                                    //権限保持

    /**
     * メンバ変数の初期化
     */
    function knjz220fModel()
    {
        $this->level = "";
    }
    function init()
    {               
        //セッション変数に保存
        $this->level = VARS::request("ASSESSLEVELCNT");
        $cnt = $this->level;

        //初期化
        $this->year    = VARS::request("YEAR");
        $this->avg     = VARS::request("AVG");
        $this->field = array();
        for($i=1;$i<=$cnt;$i++){
            $this->field["ASSESSMARK".$i] =  VARS::post("ASSESSMARK".$i);
            $this->field["ASSESSLOW".$i]  =  VARS::post("ASSESSLOW".$i);
        }
        $this->field["ASSESSHIGH".$cnt] =  VARS::post("ASSESSHIGH".$cnt);
        $this->cmd     = VARS::request("cmd");

        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = AUTHORITY;
    }

    //入力項目チェック
    function validateInput()
    {
        $chk[] = array();

        //権限チェック
        if($this->sec_competence != DEF_UPDATABLE){
            $this->setWarning("MSG300");
            return false;
        }

        //平均点チェック
        if(!isset($this->avg) || $this->avg == ""){
            $this->setWarning("MSG301","平均点");
            return false;
        }

        //評定段階数チェック
        if(!isset($this->level) || $this->level == ""){
            $this->setWarning("MSG301","評定段階数");
            return false;
        }

        //テキストエリア入力チェック
        $ii = $this->level;
        for($i=1; $i<=$ii; $i++)
        {
            if (strlen($this->field["ASSESSMARK".$i]) >6 ) {
                $this->setWarning("MSG914","(評定記号は6バイトまでです)");
            return false;  
        }
            $ASSESSLOW  = "ASSESSLOW".$i;
            if(!isset($this->field[$ASSESSLOW])){
                $this->setWarning("MSG301","下限");
                return false;
            }
            //空かどうか？
            if($this->field[$ASSESSLOW] == ""){
                $this->setWarning("MSG301","下限");
                return false;
            }

            //数字かどうか？
            if(!is_numeric($this->field[$ASSESSLOW]) && !$this->field[$ASSESSLOW] == 0){
                $this->setWarning("MSG907","下限");
                return false;
            }

            // 0より下かどうか？
            if($this->field[$ASSESSLOW] < 0){
                $this->setWarning("MSG913");
                return false;
            }

            // 100より上かどうか？
            if($this->field["ASSESSLOW".$i] > 100){
                $this->setWarning("MSG913","下限は100を超えてはいけません。");
                return false;
            }

            if($i != 1){
                $chk[] = $this->field[$ASSESSLOW];
                //値大小チェック
                if($i >= 1){
                    if($i > 2){
                       if($this->field["ASSESSLOW2"] < 1){
                   $this->setWarning("MSG913");
                           return false;
                       }
                    }
                    $ASSESSLOW_UPPER  = "ASSESSLOW".($i-1);

                    if(!($this->field[$ASSESSLOW_UPPER] < $this->field[$ASSESSLOW])){
                   $this->setWarning("MSG913");
                        return false;
                    }
                }
                    
            }
            
            if($i == $ii){
                if($this->field["ASSESSLOW".$i] > $this->field["ASSESSHIGH".$i]){
                   $this->setWarning("MSG913");
                    return false;
                }
                if($this->field["ASSESSHIGH".$i] > 100){
                    $this->setWarning("MSG913","上限は100を超えてはいけません。");
                    return false;
                }
            }
        }
        return true;
    }
        
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $overBig = $this->update();
            if($overBig){
                if ( !isset($this->warning)){
                    
                    /**
                     * 期末評定の段階数と
                       学年末評定の段階数を変える時、
                       学年末評定の上限と帳票評定の上限も変わって再度調整のメッセージを出す
                     */
                    if($overBig==3){
                        $this->setMessage("MSG201","学年末評定の上限は変わりました、学年末評定を再度調整してください");
                    }elseif($overBig==4){
                        $this->setMessage("MSG201","帳票評定の上限は変わりました、帳票評定を再度調整してください");
                    }else{
                        $this->setMessage("MSG201");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz220fQuery::getInsertQuery($this);
        return $result;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        if(!isset($this->year)){
            $this->setWarning("MSG301","年度");
            return false;
        }
        knjz220fQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $pre_year = $this->year - 1;
        $preYear_cnt = $db->getOne(knjz220fQuery::getCopyData($pre_year, "cnt"));
        //前年度データチェック
        if ($preYear_cnt == 0) {
            $this->setWarning("MSG203", "前年度データがありません。");
            return false;  
        }
        Query::dbCheckIn($db);
        knjz220fQuery::getCopyQuery($this, $pre_year);
        $this->setMessage("MSG201");
    }
}
?>
