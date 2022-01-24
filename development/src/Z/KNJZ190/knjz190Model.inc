<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}
class knjz190Model extends Model {
    var $cmd;
    var $cntl_dt_year;              //学籍処理日の年
    var $month_dat;                 //開始終了月データ
    var $cw1 ;                      //list内textboxデータ
    var $cw2 ;
    var $cw3 ;
    var $cw4 ;
    var $cd1 ;
    var $cd2 ;
    var $cd3 ;
    var $cd4 ;
    var $hr_class_dat = array();    //HR_CLASSのデータ
    var $ctrl = array();//共通関数
    var $gakki1;
    /**
     * メンバ変数の初期化
     */
    function knjz190Model()
    {
        //学期数取得,学期名, 学籍処理日取得 学期データ取得
        common::GetControlMaster_Fnc($this->ctrl);//共通関数(コントロールマスタより)

        //可変セル
        $this->gakki1 = $this->ctrl["学期数"]+"1";
        //HR_CLASSデータを取得
        $db = Query::dbCheckOut();
        $query = knjz190Query::get_ClassData(CTRL_YEAR);
        $result = $db->query($query);

        $i=1;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->hr_class_dat[$i] = $row["COUNT"];
            $i++;
        }

        $result->free();
        Query::dbCheckIn($db);

    }

    function init()
    {
        $this->cmd                          = VARS::request("cmd");
        $this->cw1                          = VARS::post("CW1");
        $this->cw2                          = VARS::post("CW2");
        $this->cw3                          = VARS::post("CW3");
        $this->cw4                          = VARS::post("CW4");

        $this->cd1                          = VARS::post("CD1");
        $this->cd2                          = VARS::post("CD2");
        $this->cd3                          = VARS::post("CD3");
        $this->cd4                          = VARS::post("CD4");

        //セキュリティチェック
        $this->staffcd = STAFFCD;
        $this->sc = common::SecurityCheck($this->staffcd,"KNJZ190");

    }

    //入力項目チェック
    function validateInput()
    {
        //処理年度チェック
        if (CTRL_YEAR == ""){
            common::ShowMessage('MSG300',"学籍処理年度と異なる年度の日付は入力できません");
            return false;
        }

        //入力権限チェック
        if ($this->sc != DEF_UPDATABLE){
            return false;
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
            if($this->update()){
                common::ShowMessage('MSG201');
            }
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz190Query::getUpdateQuery($this);
        return $result;
    }


}
?>
