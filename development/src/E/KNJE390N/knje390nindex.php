<?php

require_once('for_php7.php');

require_once('knje390nModel.inc');
require_once('knje390nQuery.inc');

class knje390nController extends Controller {
    var $ModelClassName = "knje390nModel";
    var $ProgramID      = "KNJE390N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit_change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nForm1");
                    break 2;
                //福祉サービス施設検索
                case "welfare_useservice_search":
                case "welfare_useservice_search_change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubSearch");
                    break 2;
                //検査機関検索
                case "check_center_search":
                case "check_center_search_change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubSearchCheckCenter");
                    break 2;
                //障害名・診断名マスタ、訓練内容マスタ、構成員マスタ、福祉の将来必要と考えられるサービス、医療的ケアマスタ、医療機関参照
                case "challenged_master":
                case "challenged_training_master":
                case "team_member_master":
                case "aftertime_need_service_master":
                case "medical_care_master":
                case "medical_center":
                case "checkname_master":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubMaster");
                    break 2;
                //家族構成
                case "family":
                case "familyA":
                case "family_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nFamily");
                    break 2;
                case "family_insert":
                case "family_update":
                case "family_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEtcModel();
                    $sessionInstance->setCmd("familyA");
                    break 1;
                //***** 基本情報 ****/
                case "subform1":
                case "subform1A":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm1");
                    break 2;
                //教育履歴
                case "subform1_educate":
                case "subform1_educateA":
                case "subform1_educate_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm1_2");
                    break 2;
                case "educate1_insert":
                case "educate1_update":
                case "educate1_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform1_educateA");
                    break 1;
                //医療
                case "subform1_medical":
                case "subform1_medicalA":
                case "subform1_medical_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm1_3");
                    break 2;
                case "medical1_insert":
                case "medical1_update":
                case "medical1_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform1_medicalA");
                    break 1;
                //健康管理
                case "subform1_healthcare":
                case "subform1_healthcareA":
                case "subform1_healthcare_set":
                case "subform1_healthcare_set2":
                case "subform1_healthcare_set3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm1_4");
                    break 2;
                case "healthcare1_insert":
                case "healthcare1_update":
                case "healthcare1_delete":
                case "healthcare1_insert_care":
                case "healthcare1_update_care":
                case "healthcare1_delete_care":
                case "healthcare1_insert_spasm":
                case "healthcare1_update_spasm":
                case "healthcare1_delete_spasm":
                case "healthcare1_update2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform1_healthcareA");
                    break 1;
                //福祉
                case "subform1_welfare":
                case "subform1_welfare2":
                case "subform1_welfare3":
                case "subform1_welfareA":
                case "subform1_welfare_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm1_5");
                    break 2;
                case "welfare1_insert":
                case "welfare1_update":
                case "welfare1_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform1_welfareA");
                    break 1;
                //労働
                case "subform1_work":
                case "subform1_workA":
                case "subform1_work_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm1_6");
                    break 2;
                case "work1_insert":
                case "work1_update":
                case "work1_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform1_workA");
                    break 1;
                //生育歴
                case "subform1_growth":
                case "subform1_growth2":
                case "subform1_growthA":
                case "subform1_growth_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm1_7");
                    break 2;
                case "growth1_update":
                case "growth1_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform1_growthA");
                    break 1;
                //訓練機関
                case "subform1_training":
                case "subform1_training2":
                case "subform1_trainingA":
                case "subform1_training_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm1_8");
                    break 2;
                case "training1_update":
                case "training1_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform1_trainingA");
                    break 1;
                //視力・聴力
                case "subform1_visionEar":
                case "subform1_visionEar2":
                case "subform1_visionEarA":
                case "subform1_visionEar_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm1_9");
                    break 2;
                case "visionEar1_update":
                case "visionEar1_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform1_visionEarA");
                    break 1;
                //*****A アセスメント表****/
                case "subform2_formatnew":
                case "subform2":
                case "subform2A":
                case "subform2_clear":
                case "subform2_torikomi_main"://障害名、状況取り込み
                case "subform2_yomikomi":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm2");
                    break 2;
                //発達検査
                case "subform2_check":
                case "subform2_checkA":
                case "subform2_check_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm2_2");
                    break 2;
                case "check2_insert":
                case "check2_update":
                case "check2_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform2_checkA");
                    break 1;

                //教科等の実態
                case "subform2_actual":
                case "subform2_actualA":
                case "subform2_actual_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm2_3");
                    break 2;

                case "actual2_insert":
                case "actual2_update":
                case "actual2_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateStatusSubclassModel();
                    $sessionInstance->setCmd("subform2_actualA");
                    break 1;

                //*****C 支援内容・計画****/
                case "subform3":
                case "subform3A":
                case "subform3_clear":
                case "subform3_yomikomi":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm3");
                    break 2;
                //*****D 移行支援計画****/
                case "subform4":
                case "subform4A":
                case "subform4_clear":
                case "subform4_torikomi_main"://基本情報の障害名、状況取り込み
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm4");
                    break 2;
                //発達検査
                case "subform4_check":
                case "subform4_checkA":
                case "subform4_check_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm4_2");
                    break 2;
                case "check4_insert":
                case "check4_update":
                case "check4_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform4_checkA");
                    break 1;
                //スムーズな以降に向けての支援(フィールドはmainに含まれる)
                case "subform4_smooth":
                case "subform4_smoothA":
                case "subform4_smooth_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm4_3");
                    break 2;
                case "smooth4_updatemain":
                case "smooth4_insert":
                case "smooth4_update":
                case "smooth4_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform4_smoothA");
                    break 1;
                //進路指導計画
                case "subform4_careerguidance":
                case "subform4_careerguidanceA":
                case "subform4_careerguidance_set":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm4_4");
                    break 2;
                case "careerguidance4_insert":
                case "careerguidance4_update":
                case "careerguidance4_update2":
                case "careerguidance4_delete":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform4_careerguidanceA");
                    break 1;
                //*****サポートブック****/
                case "subform5_formatnew":
                case "subform5":
                case "subform5A":
                case "subform5_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm5");
                    break 2;
                //*****引継資料****/
                case "subform6_formatnew":
                case "subform6":
                case "subform6A":
                case "subform6_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm6");
                    break 2;
                //*****事業間資料****/
                case "subform7_formatnew":
                case "subform7":
                case "subform7A":
                case "subform7_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm7");
                    break 2;
                //*****関係者間資料****/
                case "subform8_formatnew":
                case "subform8":
                case "subform8A":
                case "subform8_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje390nSubForm8");
                    break 2;
                /*case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    break 1;*/
                //メイン処理
                case "subform1_updatemain":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform1A");
                    break 1;
                case "subform1_rireki":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    //$sessionInstance->getUpdateModel();
                    $sessionInstance->getUpdateRirekiModel();
                    $sessionInstance->setCmd("subform1A");
                    break 1;
                case "subform2_updatemain":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform2A");
                    break 1;
                //case "subform2_rirekiはカット
                case "subform2_new":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateNewModel();
                    $sessionInstance->setCmd("edit_change");
                    break 1;
                case "subform3_updatemain":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform3A");
                    break 1;
                case "subform3_rireki":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    //$sessionInstance->getUpdateModel();
                    $sessionInstance->getUpdateRirekiModel();
                    $sessionInstance->setCmd("subform3A");
                    break 1;
                case "subform3_new":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateNewModel();
                    $sessionInstance->setCmd("edit_change");
                    break 1;
                case "subform4_updatemain":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform4A");
                    break 1;
                case "subform4_rireki":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    //$sessionInstance->getUpdateModel();
                    $sessionInstance->getUpdateRirekiModel();
                    $sessionInstance->setCmd("subform4A");
                    break 1;
                case "subform5_updatemain":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform5A");
                    break 1;
                case "subform5_new":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateNewModel();
                    $sessionInstance->setCmd("edit_change");
                    break 1;
                case "subform6_updatemain":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform6A");
                    break 1;
                case "subform6_new":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateNewModel();
                    $sessionInstance->setCmd("edit_change");
                    break 1;
                case "subform7_updatemain":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform7A");
                    break 1;
                case "subform7_new":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateNewModel();
                    $sessionInstance->setCmd("edit_change");
                    break 1;
                case "subform8_updatemain":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subform8A");
                    break 1;
                case "subform8_new":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateNewModel();
                    $sessionInstance->setCmd("edit_change");
                    break 1;
                /*case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/E/KNJE390N/knje390nindex.php?cmd=edit") ."&button=3" ."&SES_FLG=2";
                    $args["right_src"] = "knje390nindex.php?cmd=subform2A";
                    $args["cols"] = "22%,*";
                    View::frame($args);
                    return;*/
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&special_div=1AND2AND3&TARGET=right_frame&PATH=" .urlencode("/E/KNJE390N/knje390nindex.php?cmd=edit") ."&button=3" ."&SES_FLG=2";
                    $args["right_src"] = "knje390nindex.php?cmd=edit";
                    $args["cols"] = "22%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje390nCtl = new knje390nController;
?>
